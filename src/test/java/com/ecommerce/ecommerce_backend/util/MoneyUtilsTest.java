package com.ecommerce.ecommerce_backend.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class MoneyUtilsTest {

    @Test
    public void scaleRoundsMoneyToTwoDecimalPlaces() {

        BigDecimal result = MoneyUtils.scale(
                new BigDecimal("10.565")
        );

        Assertions.assertEquals(
                new BigDecimal("10.57"),
                result
        );
    }

    @Test
    public void calculateTotalReturnsScaledAmount() {

        BigDecimal result =
                MoneyUtils.calculateTotal(
                        new BigDecimal("10.56"),
                        4
                );

        Assertions.assertEquals(
                new BigDecimal("42.24"),
                result
        );
    }

    @Test
    public void calculateTotalHandlesTrailingZeros() {

        BigDecimal result =
                MoneyUtils.calculateTotal(
                        new BigDecimal("5"),
                        2
                );

        Assertions.assertEquals(
                new BigDecimal("10.00"),
                result
        );
    }
}