package saga.system.order_processing.shipping.model;

public enum ShipmentStatus {
    PENDING,
    ON_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED;

    public boolean canTransitionTo(ShipmentStatus next) {
        return this.ordinal() + 1 == next.ordinal();
    }
}
