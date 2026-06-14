package saga.system.order_processing.shipping.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import saga.system.order_processing.shipping.dto.ShipmentResponse;
import saga.system.order_processing.shipping.dto.UpdateShipmentRequest;
import saga.system.order_processing.shipping.service.ShippingService;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;

    @PatchMapping("/{id}")
    public ResponseEntity<ShipmentResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateShipmentRequest request) {
        return ResponseEntity.ok(shippingService.updateStatus(id, request.getStatus()));
    }
}
