package saga.system.order_processing.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private List<OrderItemRequest> items;
}
