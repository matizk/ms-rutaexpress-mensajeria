package com.rutaexpress.messaging.audit;

import com.rabbitmq.client.Channel;
import com.rutaexpress.messaging.events.EventHandler;
import java.io.IOException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AuditConsumer {
    private final EventHandler handler;
    public AuditConsumer(EventHandler handler) { this.handler = handler; }
    @RabbitListener(queues = "${app.topology.audit-queue}")
    public void receive(Message message, Channel channel) throws IOException { handler.handle("AUDIT", message, channel); }
}
