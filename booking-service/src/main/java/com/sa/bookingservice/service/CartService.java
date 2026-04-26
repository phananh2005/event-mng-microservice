package com.sa.bookingservice.service;

import com.sa.bookingservice.client.TicketServiceClient;
import com.sa.bookingservice.dto.request.CartItemRequest;
import com.sa.bookingservice.dto.response.CartResponse;
import com.sa.bookingservice.dto.response.TicketTypeResponse;
import com.sa.bookingservice.exception.AppException;
import com.sa.bookingservice.exception.ErrorCode;
import com.sa.bookingservice.mapper.CartMapper;
import com.sa.bookingservice.model.entity.Cart;
import com.sa.bookingservice.model.entity.CartItem;
import com.sa.bookingservice.model.enums.CartStatus;
import com.sa.bookingservice.repository.CartRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartService {

    CartRepository cartRepository;
    TicketServiceClient ticketServiceClient;
    CartMapper cartMapper;

    @Transactional
    public CartResponse addToCart(CartItemRequest request) {
        Long customerId = extractUserId();

        TicketTypeResponse tt = getTicketType(request.getTicketTypeId());

        if (tt.getRemainingQuantity() < request.getQuantity()) {
            throw new AppException(ErrorCode.TICKET_NOT_ENOUGH);
        }

        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> cartRepository.save(Cart.builder()
                        .customerId(customerId)
                        .status(CartStatus.ACTIVE)
                        .items(new ArrayList<>())
                        .build()));

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getTicketTypeId().equals(request.getTicketTypeId()))
                .findFirst();

        int currentInCart = existing.map(CartItem::getQuantity).orElse(0);
        int total = currentInCart + request.getQuantity();

        if (total > tt.getRemainingQuantity()) {
            throw new AppException(ErrorCode.TICKET_NOT_ENOUGH);
        }

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(total);
            item.setSubtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(total)));
        } else {
            cart.getItems().add(CartItem.builder()
                    .cart(cart)
                    .ticketTypeId(tt.getId())
                    .ticketTypeName(tt.getName())
                    .quantity(request.getQuantity())
                    .unitPrice(tt.getPrice())
                    .subtotal(tt.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())))
                    .build());
        }

        return cartMapper.toCartResponse(cartRepository.save(cart));
    }

    public CartResponse getMyCart() {
        Long customerId = extractUserId();
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> Cart.builder().items(new ArrayList<>()).build());
        return cartMapper.toCartResponse(cart);
    }

    @Transactional
    public CartResponse updateQuantity(Long itemId, Integer quantity) {
        if (quantity == null || quantity <= 0) return removeItem(itemId);

        Long customerId = extractUserId();
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_EMPTY));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        TicketTypeResponse tt = getTicketType(item.getTicketTypeId());
        if (quantity > tt.getRemainingQuantity()) {
            throw new AppException(ErrorCode.TICKET_NOT_ENOUGH);
        }

        item.setQuantity(quantity);
        item.setSubtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
        return cartMapper.toCartResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItem(Long itemId) {
        Long customerId = extractUserId();
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_EMPTY));
        cart.getItems().removeIf(i -> i.getId().equals(itemId));
        return cartMapper.toCartResponse(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart() {
        Long customerId = extractUserId();
        cartRepository.findByCustomerId(customerId).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }

    private TicketTypeResponse getTicketType(Long id) {
        try {
            return ticketServiceClient.getTicketType(id).getResult();
        } catch (Exception e) {
            throw new AppException(ErrorCode.TICKET_TYPE_NOT_FOUND);
        }
    }

    Long extractUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.parseLong(jwt.getSubject());
    }
}
