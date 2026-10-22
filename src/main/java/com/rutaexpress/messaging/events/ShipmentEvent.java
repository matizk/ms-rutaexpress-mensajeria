package com.rutaexpress.messaging.events;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ShipmentEvent(int schemaVersion, UUID eventId, String eventType, Long shipmentId,
                            String trackingCode, String status, Instant occurredAt) {
    private static final Set<String> TYPES = Set.of("shipment.created", "shipment.status_changed");
    private static final Set<String> STATUSES = Set.of("CREADO", "ACEPTADO", "EN_BODEGA", "EN_RUTA", "ENTREGADO", "CANCELADO");
    public void validate() {
        if (schemaVersion != 1 || eventId == null || eventType == null || !TYPES.contains(eventType)
                || shipmentId == null || shipmentId <= 0 || trackingCode == null
                || !trackingCode.matches("RX-[A-Z0-9-]{1,47}") || status == null || !STATUSES.contains(status)
                || occurredAt == null) {
            throw new IllegalArgumentException("Invalid shipment event contract");
        }
    }
}
