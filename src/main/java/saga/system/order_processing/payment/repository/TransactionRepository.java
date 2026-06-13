package saga.system.order_processing.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import saga.system.order_processing.payment.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
