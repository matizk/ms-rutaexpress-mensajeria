package com.rutaexpress.messaging.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rabbitmq")
public class RabbitAdminController {
    private final RabbitAdminService service;
    public RabbitAdminController(RabbitAdminService service) { this.service = service; }
    @GetMapping("/topology")
    public RabbitAdminService.TopologyStatus topology() { return service.topology(); }
    @GetMapping("/queues/{name}")
    public RabbitAdminService.QueueStatus queue(@PathVariable String name) { return service.queue(name); }
    @PostMapping("/queues")
    public ResponseEntity<Void> createQueue(@Valid @RequestBody QueueRequest body) {
        service.createQueue(body.name(), body.ttlMillis());
        return ResponseEntity.created(URI.create("/api/rabbitmq/queues/" + body.name())).build();
    }
    @PostMapping("/exchanges")
    public ResponseEntity<Void> createExchange(@Valid @RequestBody ExchangeRequest body) {
        service.createExchange(body.name(), body.type());
        return ResponseEntity.created(URI.create("/api/rabbitmq/exchanges/" + body.name())).build();
    }
    @PostMapping("/bindings")
    public ResponseEntity<Void> createBinding(@Valid @RequestBody BindingRequest body) {
        service.createBinding(body.exchange(), body.queue(), body.routingKey());
        return ResponseEntity.status(201).build();
    }
    @DeleteMapping("/bindings")
    public ResponseEntity<Void> deleteBinding(@Valid @RequestBody BindingRequest body) {
        service.deleteBinding(body.exchange(), body.queue(), body.routingKey());
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/queues/{name}")
    public ResponseEntity<Void> deleteQueue(@PathVariable String name) {
        service.deleteQueue(name); return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<Void> deleteExchange(@PathVariable String name) {
        service.deleteExchange(name); return ResponseEntity.noContent().build();
    }
    public record QueueRequest(@NotBlank @Pattern(regexp = RabbitAdminService.NAME_PATTERN) String name,
                               @Min(1) @Max(86400000) Integer ttlMillis) { }
    public record ExchangeRequest(@NotBlank @Pattern(regexp = RabbitAdminService.NAME_PATTERN) String name,
                                  @NotBlank @Pattern(regexp = "direct|topic|fanout") String type) { }
    public record BindingRequest(@NotBlank @Pattern(regexp = RabbitAdminService.NAME_PATTERN) String exchange,
                                 @NotBlank @Pattern(regexp = RabbitAdminService.NAME_PATTERN) String queue,
                                 @NotBlank @Size(max = 120) String routingKey) { }
}
