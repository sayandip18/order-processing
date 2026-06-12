package saga.system.order_processing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class OrderItemResponse {
    private String itemName;
    private BigDecimal purchasePrice;
    private Integer qty;
}
