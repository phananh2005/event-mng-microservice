package com.sa.bookingservice.faker;

import com.sa.bookingservice.model.entity.Cart;
import com.sa.bookingservice.model.enums.CartStatus;
import com.sa.bookingservice.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartSeeder {

    private final CartRepository cartRepository;

    public void seed() {
        if (cartRepository.count() > 0) return;
        cartRepository.save(Cart.builder()
                .customerId(3L)
                .status(CartStatus.ACTIVE)
                .build());
    }
}
