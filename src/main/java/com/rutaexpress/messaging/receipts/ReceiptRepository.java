package com.rutaexpress.messaging.receipts;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptRepository extends JpaRepository<Receipt, String> {
    List<Receipt> findTop100ByOrderByProcessedAtDesc();
    List<Receipt> findTop100ByTrackingCodeOrderByProcessedAtDesc(String trackingCode);
    long countByDomain(String domain);
    java.util.Optional<Receipt> findFirstByTrackingCodeAndDomainAndEventTypeAndStatusOrderByProcessedAtDesc(
            String trackingCode, String domain, String eventType, String status);
}
