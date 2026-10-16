package com.rutaexpress.messaging.notifications;

import com.rabbitmq.client.Channel;
import com.rutaexpress.messaging.events.EventHandler;
import java.io.IOException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Registers an internal notification. It does not claim to send an email. */
@Component
public class NotificationConsumer {
    private final EventHandler handler;
    public NotificationConsumer(EventHandler handler) { this.handler = handler; }
    @RabbitListener(queues = "${app.topology.notifications-queue}")
    public void receive(Message message, Channel channel) throws IOException { handler.handle("NOTIFICATION", message, channel); }
}
