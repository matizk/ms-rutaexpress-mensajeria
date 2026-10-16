package com.rutaexpress.messaging.receipts;

import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/messaging")
public class ReceiptController {
    private final ReceiptRepository repository;
    public ReceiptController(ReceiptRepository repository) { this.repository = repository; }

    @GetMapping("/receipts")
    public List<Receipt> receipts(@RequestParam(required = false) @Pattern(regexp = "RX-[A-Z0-9-]{1,47}") String trackingCode) {
        return trackingCode == null ? repository.findTop100ByOrderByProcessedAtDesc()
                : repository.findTop100ByTrackingCodeOrderByProcessedAtDesc(trackingCode);
    }

    @GetMapping("/summary")
    public Map<String, Long> summary() {
        return Map.of("notifications", repository.countByDomain("NOTIFICATION"),
                "tickets", repository.countByDomain("TICKET"), "audit", repository.countByDomain("AUDIT"));
    }
}
