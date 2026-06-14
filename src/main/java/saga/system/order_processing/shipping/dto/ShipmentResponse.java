package saga.system.order_processing.shipping.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import saga.system.order_processing.shipping.model.ShipmentStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ShipmentResponse {
    private Long id;
    private Long orderId;
    private ShipmentStatus status;
    private LocalDateTime dateOfDelivery;
}
