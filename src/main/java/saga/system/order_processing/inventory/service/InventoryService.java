package saga.system.order_processing.inventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import saga.system.order_processing.inventory.model.Reserved;
import saga.system.order_processing.inventory.model.ReservationStatus;
import saga.system.order_processing.inventory.repository.ReservedRepository;
import saga.system.order_processing.model.Item;
import saga.system.order_processing.repository.ItemRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ReservedRepository reservedRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public void expireReservations() {
        List<Reserved> expired = reservedRepository
                .findByStatusAndExpiresAtBefore(ReservationStatus.ACTIVE, LocalDateTime.now());

        if (expired.isEmpty()) return;

        for (Reserved reservation : expired) {
            Item item = itemRepository.findById(reservation.getItemId())
                    .orElseThrow(() -> new RuntimeException("Item not found during reservation expiry: " + reservation.getItemId()));

            item.setStockedQty(item.getStockedQty() + reservation.getQty());
            reservation.setStatus(ReservationStatus.EXPIRED);

            log.info("Reservation expired: orderId={}, itemId={}, qty={} restored to stock",
                    reservation.getOrderId(), reservation.getItemId(), reservation.getQty());
        }
    }

    @Transactional
    public void fulfillReservation(Long orderId) {
        List<Reserved> reservations = reservedRepository
                .findByOrderIdAndStatus(orderId, ReservationStatus.ACTIVE);

        if (reservations.isEmpty()) {
            log.warn("No active reservations found for orderId={} on payment completion", orderId);
            return;
        }

        for (Reserved reservation : reservations) {
            reservation.setQty(0);
            reservation.setStatus(ReservationStatus.FULFILLED);
        }

        log.info("Fulfilled {} reservation(s) for orderId={}", reservations.size(), orderId);
    }
}
