package saga.system.order_processing.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private static final String TOPIC = "orders";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void poll() {
        List<OutboxEvent> pending = outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        if (pending.isEmpty()) return;

        for (OutboxEvent event : pending) {
            String messageJson;
            try {
                KafkaEventMessage message = new KafkaEventMessage(
                        event.getId(),
                        event.getSagaId(),
                        event.getAggregateId(),
                        event.getAggregateType(),
                        event.getEventType(),
                        event.getCreatedAt(),
                        objectMapper.readValue(event.getPayload(), Object.class)
                );
                messageJson = objectMapper.writeValueAsString(message);
            } catch (JsonProcessingException e) {
                log.error("Failed to build Kafka message for outbox event {}, skipping", event.getId(), e);
                continue;
            }

            String key = String.valueOf(event.getAggregateId());
            try {
                kafkaTemplate.send(TOPIC, key, messageJson).get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted while sending outbox event {}, aborting poll", event.getId(), e);
                break;
            } catch (ExecutionException e) {
                log.error("Failed to send Kafka message for outbox event {}, will retry next tick", event.getId(), e.getCause());
                continue;
            }

            event.setStatus(OutboxStatus.PROCESSED);
            event.setProcessedAt(LocalDateTime.now());
            log.debug("Relayed outbox event {} (orderId={}) to topic '{}'", event.getId(), key, TOPIC);
        }
    }
}
