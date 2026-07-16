package com.ecommerce.ecommerce_backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({
        FIELD,
        PARAMETER
})
@Retention(RUNTIME)
@Constraint(
        validatedBy =
                UniqueProductIdsValidator.class
)
public @interface UniqueProductIds {

    String message()
            default "Order items must contain unique product ids";

    Class<?>[] groups()
            default {};

    Class<? extends Payload>[] payload()
            default {};
}