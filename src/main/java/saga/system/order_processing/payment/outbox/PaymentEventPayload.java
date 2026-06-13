package saga.system.order_processing.payment.outbox;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PaymentEventPayload {
    private Long orderId;
    private Long transactionId;
    private BigDecimal amount;
}
