package saga.system.order_processing.shipping.dto;

import lombok.Data;
import saga.system.order_processing.shipping.model.ShipmentStatus;

@Data
public class UpdateShipmentRequest {
    private ShipmentStatus status;
}
