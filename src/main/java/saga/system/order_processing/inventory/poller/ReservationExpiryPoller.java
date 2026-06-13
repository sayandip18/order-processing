package saga.system.order_processing.inventory.poller;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import saga.system.order_processing.inventory.service.InventoryService;

@Component
@RequiredArgsConstructor
public class ReservationExpiryPoller {

    private final InventoryService inventoryService;

    @Scheduled(fixedDelay = 10000)
    public void poll() {
        inventoryService.expireReservations();
    }
}
