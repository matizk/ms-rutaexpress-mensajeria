package com.rutaexpress.messaging.events;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.rabbitmq.client.Channel;
import com.rutaexpress.messaging.receipts.ReceiptStore;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import tools.jackson.databind.json.JsonMapper;

class EventHandlerTest {
    private final JsonMapper json = JsonMapper.builder().build();
    private final ReceiptStore store = mock(ReceiptStore.class);
    private final Channel channel = mock(Channel.class);
    private EventHandler handler;
    @BeforeEach void setUp() { handler = new EventHandler(json, store, 0); }
    @Test void acknowledgesOnlyAfterTheDatabaseOperationReturns() throws Exception {
        handler.handle("TICKET", validMessage(), channel);
        var order = inOrder(store, channel);
        order.verify(store).store(eq("TICKET"), any());
        order.verify(channel).basicAck(7, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }
    @Test void rejectsInvalidContractToDlqWithoutTouchingDatabase() throws Exception {
        handler.handle("TICKET", message("{\"schemaVersion\":99}".getBytes()), channel);
        verifyNoInteractions(store);
        verify(channel).basicNack(7, false, false);
    }
    @Test void retriesDatabaseFailureThreeTimesThenDeadLetters() throws Exception {
        when(store.store(any(), any())).thenThrow(new DataAccessResourceFailureException("offline"));
        handler.handle("AUDIT", validMessage(), channel);
        verify(store, times(3)).store(eq("AUDIT"), any());
        verify(channel).basicNack(7, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }
    @Test void acknowledgesRedeliveredEventThatWasAlreadyStored() throws Exception {
        when(store.store(any(), any())).thenReturn(false);
        handler.handle("NOTIFICATION", validMessage(), channel);
        verify(channel).basicAck(7, false);
    }
    @Test void handlesAConcurrentDuplicateAfterTheOtherTransactionCommitted() throws Exception {
        when(store.store(any(), any())).thenThrow(new DataIntegrityViolationException("duplicate"));
        when(store.alreadyProcessed(any(), any())).thenReturn(true);
        handler.handle("TICKET", validMessage(), channel);
        verify(channel).basicAck(7, false);
    }
    @Test void deadLettersUnexpectedBusinessProcessingFailure() throws Exception {
        when(store.store(any(), any())).thenThrow(new IllegalStateException("bug"));
        handler.handle("TICKET", validMessage(), channel);
        verify(channel).basicNack(7, false, false);
    }
    @Test void propagatesAckFailureSoTheBrokerCanRedeliver() throws Exception {
        doThrow(new java.io.IOException("disconnected")).when(channel).basicAck(7, false);
        org.junit.jupiter.api.Assertions.assertThrows(java.io.IOException.class,
                () -> handler.handle("TICKET", validMessage(), channel));
    }
    private Message validMessage() {
        return message(json.writeValueAsBytes(new ShipmentEvent(1, UUID.randomUUID(), "shipment.created", 1L,
                "RX-TEST-1", "CREADO", Instant.now())));
    }
    private Message message(byte[] bytes) {
        MessageProperties p = new MessageProperties(); p.setDeliveryTag(7);
        return new Message(bytes, p);
    }
}
