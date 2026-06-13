package saga.system.order_processing.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import saga.system.order_processing.payment.dto.PaymentResponse;
import saga.system.order_processing.payment.model.*;
import saga.system.order_processing.payment.outbox.PaymentEventPayload;
import saga.system.order_processing.payment.repository.IdempotencyKeyRepository;
import saga.system.order_processing.payment.repository.PaymentOutboxRepository;
import saga.system.order_processing.payment.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentOutboxRepository paymentOutboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public PaymentResponse initiatePayment(Long orderId, BigDecimal amount) {
        if (idempotencyKeyRepository.existsById(orderId)) {
            log.warn("Duplicate payment attempt for orderId={}", orderId);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Payment already initiated for order " + orderId);
        }

        IdempotencyKey key = new IdempotencyKey();
        key.setId(orderId);
        key.setStatus(IdempotencyStatus.PENDING);
        key.setCreatedAt(LocalDateTime.now());
        idempotencyKeyRepository.save(key);

        Transaction transaction = new Transaction();
        transaction.setOrderId(orderId);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setCreatedAt(LocalDateTime.now());
        Transaction saved = transactionRepository.save(transaction);

        writeOutboxEvent(orderId, saved.getId(), amount);

        log.info("Payment initiated for orderId={}, transactionId={}", orderId, saved.getId());
        return new PaymentResponse(saved.getId(), orderId, amount, TransactionStatus.PENDING);
    }

    private void writeOutboxEvent(Long orderId, Long transactionId, BigDecimal amount) {
        PaymentEventPayload payload = new PaymentEventPayload(orderId, transactionId, amount);
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payment outbox payload for order " + orderId, e);
        }

        PaymentOutboxEvent event = new PaymentOutboxEvent();
        event.setId(UUID.randomUUID());
        event.setOrderId(orderId);
        event.setEventType("PaymentCompleted");
        event.setPayload(payloadJson);
        event.setStatus(PaymentOutboxStatus.PENDING);
        event.setCreatedAt(LocalDateTime.now());
        paymentOutboxRepository.save(event);
    }
}
