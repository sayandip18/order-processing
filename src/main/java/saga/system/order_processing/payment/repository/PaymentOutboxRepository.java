package saga.system.order_processing.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import saga.system.order_processing.payment.model.PaymentOutboxEvent;
import saga.system.order_processing.payment.model.PaymentOutboxStatus;

import java.util.List;
import java.util.UUID;

public interface PaymentOutboxRepository extends JpaRepository<PaymentOutboxEvent, UUID> {
    List<PaymentOutboxEvent> findByStatusOrderByCreatedAtAsc(PaymentOutboxStatus status);
}
