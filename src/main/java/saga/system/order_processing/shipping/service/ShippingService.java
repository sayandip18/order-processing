package saga.system.order_processing.shipping.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import saga.system.order_processing.shipping.dto.ShipmentResponse;
import saga.system.order_processing.shipping.model.Shipment;
import saga.system.order_processing.shipping.model.ShipmentStatus;
import saga.system.order_processing.shipping.repository.ShipmentRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingService {

    private final ShipmentRepository shipmentRepository;

    @Transactional
    public void createShipment(Long orderId) {
        if (shipmentRepository.existsByOrderId(orderId)) {
            log.info("Shipment already exists for orderId={}, skipping", orderId);
            return;
        }
        Shipment shipment = new Shipment();
        shipment.setOrderId(orderId);
        shipment.setStatus(ShipmentStatus.PENDING);
        shipment.setCreatedAt(LocalDateTime.now());
        shipmentRepository.save(shipment);
        log.info("Created shipment for orderId={}", orderId);
    }

    @Transactional
    public ShipmentResponse updateStatus(Long id, ShipmentStatus newStatus) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Shipment not found: " + id));

        if (!shipment.getStatus().canTransitionTo(newStatus)) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(422),
                    "Invalid status transition: " + shipment.getStatus() + " -> " + newStatus);
        }

        shipment.setStatus(newStatus);
        if (newStatus == ShipmentStatus.DELIVERED) {
            shipment.setDateOfDelivery(LocalDateTime.now());
        }

        return toResponse(shipmentRepository.save(shipment));
    }

    private ShipmentResponse toResponse(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getId(),
                shipment.getOrderId(),
                shipment.getStatus(),
                shipment.getDateOfDelivery()
        );
    }
}
