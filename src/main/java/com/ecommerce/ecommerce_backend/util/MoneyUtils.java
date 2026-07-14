package com.ecommerce.ecommerce_backend.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtils {

    private static final int MONEY_SCALE = 2;

    private static final RoundingMode MONEY_ROUNDING_MODE =
            RoundingMode.HALF_UP;

    private MoneyUtils() {
    }

    public static BigDecimal scale(
            BigDecimal value
    ) {

        return value.setScale(
                MONEY_SCALE,
                MONEY_ROUNDING_MODE
        );
    }

    public static BigDecimal calculateTotal(
            BigDecimal unitPrice,
            int quantity
    ) {

        return scale(
                unitPrice.multiply(
                        BigDecimal.valueOf(quantity)
                )
        );
    }
}