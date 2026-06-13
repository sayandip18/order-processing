package saga.system.order_processing.payment.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import saga.system.order_processing.outbox.KafkaEventMessage;
import saga.system.order_processing.outbox.OrderCreatedPayload;
import saga.system.order_processing.payment.service.PaymentService;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "orders", groupId = "payment-service")
    public void consume(String message) {
        KafkaEventMessage event;
        try {
            event = objectMapper.readValue(message, KafkaEventMessage.class);
        } catch (Exception e) {
            log.error("Failed to deserialize order event, skipping: {}", message, e);
            return;
        }

        if (!"OrderCreated".equals(event.getEventType())) {
            return;
        }

        OrderCreatedPayload payload;
        try {
            payload = objectMapper.convertValue(event.getPayload(), OrderCreatedPayload.class);
        } catch (Exception e) {
            log.error("Failed to parse OrderCreated payload for eventId={}", event.getEventId(), e);
            return;
        }

        try {
            paymentService.initiatePayment(payload.getOrderId(), payload.getTotalPrice());
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                log.warn("Duplicate OrderCreated event received for orderId={}, skipping", payload.getOrderId());
            } else {
                log.error("Unexpected error initiating payment for orderId={}", payload.getOrderId(), e);
            }
        }
    }
}
