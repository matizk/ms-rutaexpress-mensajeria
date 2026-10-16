package com.rutaexpress.messaging.receipts;

import com.rutaexpress.messaging.events.ShipmentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReceiptStore {
    private final ReceiptRepository repository;
    public ReceiptStore(ReceiptRepository repository) { this.repository = repository; }

    @Transactional
    public boolean store(String domain, ShipmentEvent event) {
        if (repository.existsById(Receipt.key(domain, event.eventId()))) return false;
        repository.saveAndFlush(new Receipt(domain, event));
        return true;
    }

    @Transactional(readOnly = true)
    public boolean alreadyProcessed(String domain, ShipmentEvent event) {
        return repository.existsById(Receipt.key(domain, event.eventId()));
    }
}
