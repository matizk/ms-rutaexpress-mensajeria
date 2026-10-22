package com.rutaexpress.messaging.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
@EnableConfigurationProperties(TopologyProperties.class)
public class RabbitTopology {
    @Bean
    Declarables businessTopology(TopologyProperties p) {
        TopicExchange events = new TopicExchange(p.eventsExchange(), true, false);
        DirectExchange commands = new DirectExchange(p.commandsExchange(), true, false);
        DirectExchange dlx = new DirectExchange(p.deadLetterExchange(), true, false);
        Queue notifications = mainQueue(p.notificationsQueue(), p.notificationsDlq(), p.deadLetterExchange());
        Queue tickets = mainQueue(p.ticketsQueue(), p.ticketsDlq(), p.deadLetterExchange());
        Queue audit = mainQueue(p.auditQueue(), p.auditDlq(), p.deadLetterExchange());
        Queue notificationsDlq = QueueBuilder.durable(p.notificationsDlq()).build();
        Queue ticketsDlq = QueueBuilder.durable(p.ticketsDlq()).build();
        Queue auditDlq = QueueBuilder.durable(p.auditDlq()).build();
        return new Declarables(events, commands, dlx, notifications, tickets, audit,
                notificationsDlq, ticketsDlq, auditDlq,
                BindingBuilder.bind(notifications).to(events).with(p.notificationsKey()),
                BindingBuilder.bind(audit).to(events).with(p.auditKey()),
                BindingBuilder.bind(tickets).to(commands).with(p.ticketsKey()),
                BindingBuilder.bind(notificationsDlq).to(dlx).with(p.notificationsDlq()),
                BindingBuilder.bind(ticketsDlq).to(dlx).with(p.ticketsDlq()),
                BindingBuilder.bind(auditDlq).to(dlx).with(p.auditDlq()));
    }

    private Queue mainQueue(String name, String dlq, String dlx) {
        return QueueBuilder.durable(name).withArgument("x-dead-letter-exchange", dlx)
                .withArgument("x-dead-letter-routing-key", dlq)
                .withArgument("x-message-ttl", 86400000).build();
    }
}
