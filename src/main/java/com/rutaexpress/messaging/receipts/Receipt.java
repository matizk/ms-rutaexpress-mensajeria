package com.rutaexpress.messaging.receipts;

import com.rutaexpress.messaging.events.ShipmentEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "message_receipts", indexes = @Index(name = "ix_receipt_tracking", columnList = "tracking_code"))
public class Receipt {
    @Id @Column(length = 80) private String id;
    @Column(nullable = false) private UUID eventId;
    @Column(nullable = false, length = 20) private String domain;
    @Column(name = "tracking_code", nullable = false, length = 50) private String trackingCode;
    @Column(nullable = false, length = 60) private String eventType;
    @Column(nullable = false, length = 20) private String status;
    @Column(nullable = false) private Instant occurredAt;
    @Column(nullable = false) private Instant processedAt;

    protected Receipt() { }
    public Receipt(String domain, ShipmentEvent event) {
        this.id = key(domain, event.eventId());
        this.eventId = event.eventId();
        this.domain = domain;
        this.trackingCode = event.trackingCode();
        this.eventType = event.eventType();
        this.status = event.status();
        this.occurredAt = event.occurredAt();
        this.processedAt = Instant.now();
    }
    public static String key(String domain, UUID eventId) { return domain + ":" + eventId; }
    public String getId() { return id; }
    public UUID getEventId() { return eventId; }
    public String getDomain() { return domain; }
    public String getTrackingCode() { return trackingCode; }
    public String getEventType() { return eventType; }
    public String getStatus() { return status; }
    public Instant getOccurredAt() { return occurredAt; }
    public Instant getProcessedAt() { return processedAt; }
}
