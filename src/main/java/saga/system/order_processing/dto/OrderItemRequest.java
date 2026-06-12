package saga.system.order_processing.dto;

import lombok.Data;

@Data
public class OrderItemRequest {
    private Long itemId;
    private Integer qty;
}
