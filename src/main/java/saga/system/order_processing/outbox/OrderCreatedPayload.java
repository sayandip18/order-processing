package saga.system.order_processing.outbox;

import lombok.AllArgsConstructor;
import lombok.Data;
import saga.system.order_processing.dto.OrderItemResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderCreatedPayload {
    private Long orderId;
    private Long userId;
    private String userEmail;
    private List<OrderItemResponse> items;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
}
