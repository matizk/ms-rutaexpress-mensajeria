package com.rutaexpress.messaging.admin;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import com.rutaexpress.messaging.config.TopologyProperties;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.web.server.ResponseStatusException;

class RabbitAdminServiceTest {
    private final AmqpAdmin admin = mock(AmqpAdmin.class);
    private final RabbitAdminService service = new RabbitAdminService(admin, new TopologyProperties(
            "rutaexpress.events", "rutaexpress.commands", "rutaexpress.dlx", "rutaexpress.notifications", "rutaexpress.tickets",
            "rutaexpress.audit", "rutaexpress.notifications.dlq", "rutaexpress.tickets.dlq", "rutaexpress.audit.dlq", "shipment.*", "shipment.#", "ticket.generate"));
    @Test void protectsManagedBusinessQueues() {
        assertThrows(ResponseStatusException.class, () -> service.deleteQueue("rutaexpress.tickets"));
        verifyNoInteractions(admin);
    }
    @Test void refusesToDeleteMessages() {
        when(admin.getQueueInfo("rutaexpress.demo")).thenReturn(new QueueInformation("rutaexpress.demo", 2, 0));
        assertThrows(ResponseStatusException.class, () -> service.deleteQueue("rutaexpress.demo"));
        verify(admin, never()).deleteQueue(anyString(), anyBoolean(), anyBoolean());
    }
    @Test void keepsBrokerSideChecksWhenDeletingEmptyQueue() {
        when(admin.getQueueInfo("rutaexpress.demo")).thenReturn(new QueueInformation("rutaexpress.demo", 0, 0));
        service.deleteQueue("rutaexpress.demo");
        verify(admin).deleteQueue("rutaexpress.demo", true, true);
    }
    @Test void rejectsOtherApplicationNamespaces() {
        assertThrows(IllegalArgumentException.class, () -> service.createQueue("other.application", null));
        verifyNoInteractions(admin);
    }
}
