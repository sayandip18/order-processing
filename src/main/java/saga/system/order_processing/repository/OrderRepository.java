package saga.system.order_processing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import saga.system.order_processing.model.Order;
import saga.system.order_processing.model.User;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
}
