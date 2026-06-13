package saga.system.order_processing.outbox;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class KafkaEventMessage {
    private UUID eventId;
    private Long sagaId;
    private Long aggregateId;
    private String aggregateType;
    private String eventType;
    private LocalDateTime occurredAt;
    private Object payload;
}
