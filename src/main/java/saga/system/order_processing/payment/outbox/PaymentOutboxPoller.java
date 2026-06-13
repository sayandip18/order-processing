package saga.system.order_processing.payment.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import saga.system.order_processing.outbox.KafkaEventMessage;
import saga.system.order_processing.payment.model.PaymentOutboxEvent;
import saga.system.order_processing.payment.model.PaymentOutboxStatus;
import saga.system.order_processing.payment.repository.PaymentOutboxRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOutboxPoller {

    private static final String TOPIC = "payments";

    private final PaymentOutboxRepository paymentOutboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void poll() {
        List<PaymentOutboxEvent> pending = paymentOutboxRepository
                .findByStatusOrderByCreatedAtAsc(PaymentOutboxStatus.PENDING);
        if (pending.isEmpty()) return;

        for (PaymentOutboxEvent event : pending) {
            String messageJson;
            try {
                KafkaEventMessage message = new KafkaEventMessage(
                        event.getId(),
                        event.getOrderId(),
                        event.getOrderId(),
                        "Payment",
                        event.getEventType(),
                        event.getCreatedAt(),
                        objectMapper.readValue(event.getPayload(), Object.class)
                );
                messageJson = objectMapper.writeValueAsString(message);
            } catch (JsonProcessingException e) {
                log.error("Failed to build Kafka message for payment outbox event {}, skipping", event.getId(), e);
                continue;
            }

            String key = String.valueOf(event.getOrderId());
            try {
                kafkaTemplate.send(TOPIC, key, messageJson).get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted while sending payment outbox event {}, aborting poll", event.getId(), e);
                break;
            } catch (ExecutionException e) {
                log.error("Failed to send Kafka message for payment outbox event {}, will retry next tick", event.getId(), e.getCause());
                continue;
            }

            event.setStatus(PaymentOutboxStatus.PROCESSED);
            event.setProcessedAt(LocalDateTime.now());
            log.debug("Relayed payment outbox event {} (orderId={}) to topic '{}'", event.getId(), key, TOPIC);
        }
    }
}
