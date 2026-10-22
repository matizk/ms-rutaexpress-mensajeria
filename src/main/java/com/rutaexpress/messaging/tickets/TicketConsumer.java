package com.rutaexpress.messaging.tickets;

import com.rabbitmq.client.Channel;
import com.rutaexpress.messaging.events.EventHandler;
import java.io.IOException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TicketConsumer {
    private final EventHandler handler;
    public TicketConsumer(EventHandler handler) { this.handler = handler; }
    @RabbitListener(queues = "${app.topology.tickets-queue}")
    public void receive(Message message, Channel channel) throws IOException { handler.handle("TICKET", message, channel); }
}
