package saga.system.order_processing.shipping.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import saga.system.order_processing.shipping.model.Shipment;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    boolean existsByOrderId(Long orderId);
}
