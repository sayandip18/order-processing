package saga.system.order_processing.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import saga.system.order_processing.payment.model.IdempotencyKey;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {
}
