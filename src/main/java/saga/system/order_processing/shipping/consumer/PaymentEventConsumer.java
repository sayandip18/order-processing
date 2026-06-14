package saga.system.order_processing.shipping.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import saga.system.order_processing.outbox.KafkaEventMessage;
import saga.system.order_processing.payment.outbox.PaymentEventPayload;
import saga.system.order_processing.shipping.service.ShippingService;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final ShippingService shippingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payments", groupId = "shipping-service")
    public void consume(String message) {
        KafkaEventMessage event;
        try {
            event = objectMapper.readValue(message, KafkaEventMessage.class);
        } catch (Exception e) {
            log.error("Failed to deserialize payment event, skipping: {}", message, e);
            return;
        }

        if (!"PaymentCompleted".equals(event.getEventType())) {
            return;
        }

        PaymentEventPayload payload;
        try {
            payload = objectMapper.convertValue(event.getPayload(), PaymentEventPayload.class);
        } catch (Exception e) {
            log.error("Failed to parse PaymentCompleted payload for eventId={}", event.getEventId(), e);
            return;
        }

        shippingService.createShipment(payload.getOrderId());
    }
}
