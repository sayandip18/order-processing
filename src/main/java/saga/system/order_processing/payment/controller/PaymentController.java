package saga.system.order_processing.payment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import saga.system.order_processing.payment.dto.PaymentRequest;
import saga.system.order_processing.payment.dto.PaymentResponse;
import saga.system.order_processing.payment.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> initiatePayment(@RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.initiatePayment(request.getOrderId(), request.getAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
