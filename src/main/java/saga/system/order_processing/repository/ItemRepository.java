package saga.system.order_processing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import saga.system.order_processing.model.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
