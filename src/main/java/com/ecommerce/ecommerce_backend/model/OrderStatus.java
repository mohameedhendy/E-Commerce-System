package com.ecommerce.ecommerce_backend.model;

public enum OrderStatus {

    PENDING,
    CONFIRMED,
    CANCELLED;

    public boolean canTransitionTo(
            OrderStatus targetStatus) {

        if (targetStatus == null) {
            return false;
        }

        return switch (this) {
            case PENDING ->
                    targetStatus == CONFIRMED
                            || targetStatus == CANCELLED;

            case CONFIRMED ->
                    targetStatus == CANCELLED;

            case CANCELLED -> false;
        };
    }
}