package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dao.CartDao;
import com.ecommerce.ecommerce_backend.dto.CartResponse;
import com.ecommerce.ecommerce_backend.model.LocalUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartDao cartDao;

    @Transactional(readOnly = true)
    public CartResponse getCart(LocalUser currentUser) {

        return cartDao
                .findDetailedByUserId(
                        currentUser.getId()
                )
                .map(CartResponse::new)
                .orElseGet(CartResponse::empty);
    }
}