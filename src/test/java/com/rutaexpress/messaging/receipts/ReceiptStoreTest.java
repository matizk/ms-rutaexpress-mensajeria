package com.rutaexpress.messaging.receipts;

import static org.junit.jupiter.api.Assertions.*;
import com.rutaexpress.messaging.events.ShipmentEvent;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest(properties = "spring.datasource.password=")
@Import(ReceiptStore.class)
class ReceiptStoreTest {
    @Autowired ReceiptStore store;
    @Autowired ReceiptRepository repository;
    @Test void deduplicatesPerDomainButKeepsEachIndependentConsumerResult() {
        ShipmentEvent event = new ShipmentEvent(1, UUID.randomUUID(), "shipment.created", 1L, "RX-TEST-1", "CREADO", Instant.now());
        assertTrue(store.store("TICKET", event));
        assertFalse(store.store("TICKET", event));
        assertTrue(store.store("AUDIT", event));
        assertEquals(2, repository.count());
    }
    @Test void confirmationQueryNeverMistakesCreationOrLaterDeliveryForAcceptance() {
        String code = "RX-CONFIRM-TEST";
        store.store("TICKET", new ShipmentEvent(1, UUID.randomUUID(), "shipment.created", 2L, code, "CREADO", Instant.now()));
        assertTrue(repository.findFirstByTrackingCodeAndDomainAndEventTypeAndStatusOrderByProcessedAtDesc(
                code, "TICKET", "shipment.status_changed", "ACEPTADO").isEmpty());
        var event = new ShipmentEvent(1, UUID.randomUUID(), "shipment.status_changed", 2L, code, "ACEPTADO", Instant.now());
        store.store("TICKET", event);
        store.store("TICKET", new ShipmentEvent(1, UUID.randomUUID(), "shipment.status_changed", 2L, code, "ENTREGADO", Instant.now()));
        var receipt = repository.findFirstByTrackingCodeAndDomainAndEventTypeAndStatusOrderByProcessedAtDesc(
                code, "TICKET", "shipment.status_changed", "ACEPTADO").orElseThrow();
        assertEquals(event.eventId(), receipt.getEventId());
        assertEquals("ACEPTADO", receipt.getStatus());
    }
}
