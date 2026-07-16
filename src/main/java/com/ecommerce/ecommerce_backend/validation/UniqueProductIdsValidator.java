package com.ecommerce.ecommerce_backend.validation;

import com.ecommerce.ecommerce_backend.dto.OrderItemRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UniqueProductIdsValidator
        implements ConstraintValidator<
        UniqueProductIds,
        List<OrderItemRequest>
        > {

    @Override
    public boolean isValid(
            List<OrderItemRequest> items,
            ConstraintValidatorContext context
    ) {

        if (items == null
                || items.isEmpty()) {

            return true;
        }

        Set<Long> productIds =
                new HashSet<>();

        for (OrderItemRequest item : items) {

            if (item == null
                    || item.getProductId() == null) {

                continue;
            }

            if (!productIds.add(
                    item.getProductId()
            )) {
                return false;
            }
        }

        return true;
    }
}