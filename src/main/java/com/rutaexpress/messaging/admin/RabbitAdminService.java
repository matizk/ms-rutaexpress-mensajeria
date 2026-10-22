package com.rutaexpress.messaging.admin;

import com.rutaexpress.messaging.config.TopologyProperties;
import java.util.List;
import java.util.Map;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RabbitAdminService {
    public static final String NAME_PATTERN = "rutaexpress\\.[a-zA-Z0-9][a-zA-Z0-9_.-]{0,99}";
    private final AmqpAdmin admin;
    private final TopologyProperties topology;
    public RabbitAdminService(AmqpAdmin admin, TopologyProperties topology) { this.admin = admin; this.topology = topology; }

    public void createQueue(String name, Integer ttlMillis) {
        assertMutable(name);
        QueueBuilder builder = QueueBuilder.durable(name);
        if (ttlMillis != null) builder.withArgument("x-message-ttl", ttlMillis);
        admin.declareQueue(builder.build());
    }
    public void createExchange(String name, String type) {
        assertMutable(name);
        Exchange exchange = switch (type) {
            case "direct" -> new DirectExchange(name, true, false);
            case "topic" -> new TopicExchange(name, true, false);
            case "fanout" -> new FanoutExchange(name, true, false);
            default -> throw new IllegalArgumentException("Tipo de exchange inválido");
        };
        admin.declareExchange(exchange);
    }
    public void createBinding(String source, String destination, String key) {
        assertMutable(source);
        assertMutable(destination);
        admin.declareBinding(binding(source, destination, key));
    }
    public void deleteBinding(String source, String destination, String key) {
        assertMutable(source);
        assertMutable(destination);
        admin.removeBinding(binding(source, destination, key));
    }
    public void deleteQueue(String name) {
        assertMutable(name);
        QueueInformation info = admin.getQueueInfo(name);
        if (info == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La cola no existe");
        if (info.getMessageCount() != 0 || info.getConsumerCount() != 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se eliminan colas vacías y sin consumidores");
        }
        // Broker-side checks also protect against a message or consumer arriving between calls.
        admin.deleteQueue(name, true, true);
    }
    public void deleteExchange(String name) {
        assertMutable(name);
        admin.deleteExchange(name);
    }
    public QueueStatus queue(String name) {
        assertNamespace(name);
        QueueInformation info = admin.getQueueInfo(name);
        if (info == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La cola no existe");
        return new QueueStatus(name, info.getMessageCount(), info.getConsumerCount());
    }
    public TopologyStatus topology() {
        List<QueueStatus> queues = topology.queueNames().stream().map(this::queue).toList();
        return new TopologyStatus(queues, Map.of(topology.eventsExchange(), "topic", topology.commandsExchange(), "direct",
                topology.deadLetterExchange(), "direct"));
    }
    private Binding binding(String source, String destination, String key) {
        if (key == null || key.isBlank() || key.length() > 120) throw new IllegalArgumentException("Routing key inválida");
        return new Binding(destination, Binding.DestinationType.QUEUE, source, key, null);
    }
    private void assertNamespace(String name) {
        if (name == null || !name.matches(NAME_PATTERN)) throw new IllegalArgumentException("Usa nombres dentro de rutaexpress.");
    }
    private void assertMutable(String name) {
        assertNamespace(name);
        if (topology.queueNames().contains(name) || topology.exchangeNames().contains(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La topología base se mantiene desde configuración");
        }
    }
    public record QueueStatus(String name, long messages, int consumers) { }
    public record TopologyStatus(List<QueueStatus> queues, Map<String, String> exchanges) { }
}
