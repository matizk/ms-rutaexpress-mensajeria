package com.rutaexpress.messaging.receipts;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operator/confirmations")
public class OperatorConfirmationController {
    private final ReceiptRepository repository;
    public OperatorConfirmationController(ReceiptRepository repository) { this.repository = repository; }

    @GetMapping("/{trackingCode}")
    public ResponseEntity<?> confirmation(@PathVariable String trackingCode) {
        if (!trackingCode.matches("RX-[A-Z0-9-]{1,47}")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Código de seguimiento inválido"));
        }
        return repository.findFirstByTrackingCodeAndDomainAndEventTypeAndStatusOrderByProcessedAtDesc(
                        trackingCode, "TICKET", "shipment.status_changed", "ACEPTADO")
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
