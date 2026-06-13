package saga.system.order_processing.payment.model;

public enum IdempotencyStatus {
    PENDING, PROCESSED, FAILED
}
