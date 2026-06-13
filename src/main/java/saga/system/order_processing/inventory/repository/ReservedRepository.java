package saga.system.order_processing.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import saga.system.order_processing.inventory.model.Reserved;
import saga.system.order_processing.inventory.model.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservedRepository extends JpaRepository<Reserved, Long> {

    List<Reserved> findByStatusAndExpiresAtBefore(ReservationStatus status, LocalDateTime threshold);

    List<Reserved> findByOrderIdAndStatus(Long orderId, ReservationStatus status);
}
