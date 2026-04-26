package com.sa.bookingservice.service;

import com.sa.bookingservice.client.TicketServiceClient;
import com.sa.bookingservice.dto.response.ApiResponse;
import com.sa.bookingservice.dto.response.OrderResponse;
import com.sa.bookingservice.exception.AppException;
import com.sa.bookingservice.exception.ErrorCode;
import com.sa.bookingservice.mapper.OrderMapper;
import com.sa.bookingservice.model.entity.Cart;
import com.sa.bookingservice.model.entity.CartItem;
import com.sa.bookingservice.model.entity.Order;
import com.sa.bookingservice.model.enums.OrderStatus;
import com.sa.bookingservice.model.enums.PaymentMethod;
import com.sa.bookingservice.model.enums.PaymentStatus;
import com.sa.bookingservice.repository.CartRepository;
import com.sa.bookingservice.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock CartRepository cartRepository;
    @Mock TicketServiceClient ticketServiceClient;
    @Mock OrderMapper orderMapper;
    @InjectMocks OrderService orderService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void setAuth(Long userId) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS512")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(jwt, null, List.of()));
    }

    private CartItem cartItem(Long ticketTypeId, int qty, BigDecimal price) {
        return CartItem.builder()
                .id(ticketTypeId)
                .ticketTypeId(ticketTypeId)
                .ticketTypeName("VIP")
                .quantity(qty)
                .unitPrice(price)
                .subtotal(price.multiply(BigDecimal.valueOf(qty)))
                .build();
    }

    @Test
    void checkout_shouldCreateConfirmedOrder() {
        setAuth(1L);
        CartItem item = cartItem(10L, 2, BigDecimal.valueOf(100));
        Cart cart = Cart.builder().id(1L).customerId(1L).items(new ArrayList<>(List.of(item))).build();

        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(ticketServiceClient.reserve(anyMap())).thenReturn(ApiResponse.<Void>builder().build());
        when(ticketServiceClient.issue(anyMap())).thenReturn(ApiResponse.<Void>builder().build());

        Order savedOrder = Order.builder().id(99L)
                .paymentStatus(PaymentStatus.PAID).orderStatus(OrderStatus.CONFIRMED)
                .totalAmount(BigDecimal.valueOf(200)).build();
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(cartRepository.save(any())).thenReturn(cart);

        OrderResponse expected = OrderResponse.builder().id(99L)
                .orderStatus(OrderStatus.CONFIRMED).build();
        when(orderMapper.toOrderResponse(savedOrder)).thenReturn(expected);

        OrderResponse result = orderService.checkout(PaymentMethod.BANKING);

        assertEquals(99L, result.getId());
        assertEquals(OrderStatus.CONFIRMED, result.getOrderStatus());
        verify(ticketServiceClient).reserve(anyMap());
        verify(ticketServiceClient).issue(anyMap());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void checkout_shouldRollbackReserveWhenSecondReserveFails() {
        setAuth(1L);
        CartItem item1 = cartItem(10L, 1, BigDecimal.valueOf(100));
        CartItem item2 = cartItem(20L, 1, BigDecimal.valueOf(200));
        Cart cart = Cart.builder().id(1L).customerId(1L)
                .items(new ArrayList<>(List.of(item1, item2))).build();

        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        // first reserve succeeds, second throws
        when(ticketServiceClient.reserve(anyMap()))
                .thenReturn(ApiResponse.<Void>builder().build())
                .thenThrow(new RuntimeException("out of stock"));
        when(ticketServiceClient.release(anyMap())).thenReturn(ApiResponse.<Void>builder().build());

        AppException ex = assertThrows(AppException.class,
                () -> orderService.checkout(PaymentMethod.BANKING));
        assertEquals(ErrorCode.TICKET_NOT_ENOUGH, ex.getErrorCode());

        // release called once for the first successfully reserved item
        verify(ticketServiceClient, times(1)).release(anyMap());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void checkout_shouldThrowWhenCartEmpty() {
        setAuth(1L);
        Cart cart = Cart.builder().id(1L).customerId(1L).items(new ArrayList<>()).build();
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));

        AppException ex = assertThrows(AppException.class,
                () -> orderService.checkout(PaymentMethod.BANKING));
        assertEquals(ErrorCode.CART_EMPTY, ex.getErrorCode());
    }

    @Test
    void checkout_shouldThrowWhenCartNotFound() {
        setAuth(1L);
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> orderService.checkout(PaymentMethod.BANKING));
        assertEquals(ErrorCode.CART_EMPTY, ex.getErrorCode());
    }
}
