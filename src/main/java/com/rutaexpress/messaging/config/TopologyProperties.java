package com.rutaexpress.messaging.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.topology")
public record TopologyProperties(String eventsExchange, String commandsExchange, String deadLetterExchange,
        String notificationsQueue, String ticketsQueue, String auditQueue,
        String notificationsDlq, String ticketsDlq, String auditDlq,
        String notificationsKey, String auditKey, String ticketsKey) {
    public List<String> queueNames() {
        return List.of(notificationsQueue, ticketsQueue, auditQueue, notificationsDlq, ticketsDlq, auditDlq);
    }
    public List<String> exchangeNames() { return List.of(eventsExchange, commandsExchange, deadLetterExchange); }
}
