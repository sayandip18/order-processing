package saga.system.order_processing.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import saga.system.order_processing.payment.model.TransactionStatus;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private Long transactionId;
    private Long orderId;
    private BigDecimal amount;
    private TransactionStatus status;
}
