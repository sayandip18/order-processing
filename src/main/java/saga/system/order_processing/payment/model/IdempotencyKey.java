package saga.system.order_processing.payment.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKey {

    @Id
    private Long id; // orderId is the idempotency key

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdempotencyStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
