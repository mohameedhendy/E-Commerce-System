package com.ecommerce.ecommerce_backend.validation;

import com.ecommerce.ecommerce_backend.dto.OrderItemRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class UniqueProductIdsValidatorTest {

    private final UniqueProductIdsValidator validator =
            new UniqueProductIdsValidator();

    @Test
    public void nullItemsAreValid() {

        assertThat(
                validator.isValid(
                        null,
                        null
                )
        ).isTrue();
    }

    @Test
    public void uniqueProductIdsAreValid() {

        List<OrderItemRequest> items =
                List.of(
                        createItem(1L),
                        createItem(2L)
                );

        assertThat(
                validator.isValid(
                        items,
                        null
                )
        ).isTrue();
    }

    @Test
    public void duplicateProductIdsAreInvalid() {

        List<OrderItemRequest> items =
                List.of(
                        createItem(1L),
                        createItem(1L)
                );

        assertThat(
                validator.isValid(
                        items,
                        null
                )
        ).isFalse();
    }

    @Test
    public void nullProductIdsAreHandledByFieldValidation() {

        List<OrderItemRequest> items =
                List.of(
                        createItem(null),
                        createItem(null)
                );

        assertThat(
                validator.isValid(
                        items,
                        null
                )
        ).isTrue();
    }

    private OrderItemRequest createItem(
            Long productId
    ) {

        OrderItemRequest item =
                new OrderItemRequest();

        item.setProductId(productId);
        item.setQuantity(1);

        return item;
    }
}