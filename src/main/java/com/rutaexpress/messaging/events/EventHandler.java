package com.rutaexpress.messaging.events;

import com.rabbitmq.client.Channel;
import com.rutaexpress.messaging.receipts.ReceiptStore;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class EventHandler {
    private static final Logger log = LoggerFactory.getLogger(EventHandler.class);
    private final ObjectMapper json;
    private final ReceiptStore receipts;
    private final long retryDelay;
    public EventHandler(ObjectMapper json, ReceiptStore receipts,
                        @Value("${app.messaging.retry-delay-ms:200}") long retryDelay) {
        this.json = json;
        this.receipts = receipts;
        this.retryDelay = retryDelay;
    }

    public void handle(String domain, Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        ShipmentEvent event;
        try {
            event = json.readValue(message.getBody(), ShipmentEvent.class);
            if (event == null) throw new IllegalArgumentException("Null event");
            event.validate();
        } catch (Exception invalid) {
            log.warn("EVENT_DLQ domain={} messageId={} reason=invalid_contract", domain, message.getMessageProperties().getMessageId());
            channel.basicNack(tag, false, false);
            return;
        }

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                persistIdempotently(domain, event); // Spring commits before returning.
            } catch (DataAccessException unavailable) {
                if (attempt == 3) {
                    log.warn("EVENT_DLQ domain={} eventId={} reason=database_retries_exhausted", domain, event.eventId());
                    channel.basicNack(tag, false, false);
                    return;
                }
                log.warn("EVENT_RETRY domain={} eventId={} attempt={}", domain, event.eventId(), attempt);
                try { Thread.sleep(retryDelay * attempt); }
                catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    channel.basicNack(tag, false, true);
                    return;
                }
                continue;
            } catch (RuntimeException processingFailure) {
                log.warn("EVENT_DLQ domain={} eventId={} reason=processing_failure", domain, event.eventId());
                channel.basicNack(tag, false, false);
                return;
            }
            // ACK failures are propagated, so an already committed event can be safely redelivered.
            channel.basicAck(tag, false);
            log.info("EVENT_ACK domain={} eventId={}", domain, event.eventId());
            return;
        }
    }

    private void persistIdempotently(String domain, ShipmentEvent event) {
        try { receipts.store(domain, event); }
        catch (DataIntegrityViolationException duplicate) {
            // The failed write transaction has rolled back before this separate lookup.
            if (!receipts.alreadyProcessed(domain, event)) throw duplicate;
        }
    }
}
