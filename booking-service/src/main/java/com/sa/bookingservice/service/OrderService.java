package com.sa.bookingservice.service;

import com.sa.bookingservice.client.TicketServiceClient;
import com.sa.bookingservice.dto.response.OrderResponse;
import com.sa.bookingservice.exception.AppException;
import com.sa.bookingservice.exception.ErrorCode;
import com.sa.bookingservice.mapper.OrderMapper;
import com.sa.bookingservice.model.entity.Cart;
import com.sa.bookingservice.model.entity.CartItem;
import com.sa.bookingservice.model.entity.Order;
import com.sa.bookingservice.model.entity.OrderItem;
import com.sa.bookingservice.model.enums.OrderStatus;
import com.sa.bookingservice.model.enums.PaymentMethod;
import com.sa.bookingservice.model.enums.PaymentStatus;
import com.sa.bookingservice.repository.CartRepository;
import com.sa.bookingservice.repository.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {

    OrderRepository orderRepository;
    CartRepository cartRepository;
    TicketServiceClient ticketServiceClient;
    OrderMapper orderMapper;

    private static final float PLATFORM_FEE_RATE = 0.25f;

    @Transactional
    public OrderResponse checkout(PaymentMethod paymentMethod) {
        Long customerId = extractUserId();
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_EMPTY));
        if (cart.getItems().isEmpty()) throw new AppException(ErrorCode.CART_EMPTY);
        return createOrder(customerId, cart, cart.getItems(), paymentMethod);
    }

    @Transactional
    public OrderResponse checkoutSelected(List<Long> itemIds, PaymentMethod paymentMethod) {
        Long customerId = extractUserId();
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_EMPTY));
        List<CartItem> selected = cart.getItems().stream()
                .filter(i -> itemIds.contains(i.getId()))
                .toList();
        if (selected.isEmpty()) throw new AppException(ErrorCode.CART_EMPTY);
        return createOrder(customerId, cart, selected, paymentMethod);
    }

    public Page<OrderResponse> getMyOrders(PageRequest pageRequest) {
        Long customerId = extractUserId();
        return orderRepository.findByCustomerId(customerId, pageRequest)
                .map(orderMapper::toOrderResponse);
    }

    private OrderResponse createOrder(Long customerId, Cart cart,
                                      Collection<CartItem> items, PaymentMethod paymentMethod) {
        BigDecimal total = items.stream().map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal serviceFee = total.multiply(BigDecimal.valueOf(PLATFORM_FEE_RATE));
        BigDecimal organizerAmount = total.subtract(serviceFee);

        // 1. Reserve stock in ticket-service (throws if not enough)
        List<CartItem> reserved = new ArrayList<>();
        try {
            for (CartItem item : items) {
                ticketServiceClient.reserve(Map.of(
                        "ticketTypeId", item.getTicketTypeId(),
                        "quantity", item.getQuantity()));
                reserved.add(item);
            }
        } catch (Exception e) {
            // Rollback already-reserved items
            reserved.forEach(r -> {
                try {
                    ticketServiceClient.release(Map.of(
                            "ticketTypeId", r.getTicketTypeId(),
                            "quantity", r.getQuantity()));
                } catch (Exception ex) {
                    log.error("Failed to release stock for ticketTypeId={}", r.getTicketTypeId(), ex);
                }
            });
            throw new AppException(ErrorCode.TICKET_NOT_ENOUGH);
        }

        // 2. Persist order
        Order order = Order.builder()
                .customerId(customerId)
                .totalAmount(total)
                .serviceFee(serviceFee)
                .organizerAmount(organizerAmount)
                .platformFeeRate(PLATFORM_FEE_RATE)
                .paymentMethod(paymentMethod)
                .paymentStatus(PaymentStatus.PAID)   // simulated payment
                .orderStatus(OrderStatus.CONFIRMED)
                .orderDate(LocalDateTime.now())
                .paidAt(LocalDateTime.now())
                .build();

        List<OrderItem> orderItems = items.stream()
                .map(ci -> OrderItem.builder()
                        .order(order)
                        .ticketTypeId(ci.getTicketTypeId())
                        .ticketTypeName(ci.getTicketTypeName())
                        .quantity(ci.getQuantity())
                        .unitPrice(ci.getUnitPrice())
                        .subtotal(ci.getSubtotal())
                        .build())
                .toList();
        order.setItems(new ArrayList<>(orderItems));
        Order saved = orderRepository.save(order);

        // 3. Issue tickets in ticket-service
        for (CartItem item : items) {
            try {
                ticketServiceClient.issue(Map.of(
                        "orderId", saved.getId(),
                        "customerId", customerId,
                        "ticketTypeId", item.getTicketTypeId(),
                        "quantity", item.getQuantity()));
            } catch (Exception e) {
                log.error("Ticket issuance failed for orderId={}, ticketTypeId={}",
                        saved.getId(), item.getTicketTypeId(), e);
                // Order is confirmed; tickets can be re-issued manually — do not rollback order
            }
        }

        // 4. Remove checked-out items from cart
        cart.getItems().removeAll(items instanceof List ? (List<CartItem>) items
                : new ArrayList<>(items));
        cartRepository.save(cart);

        return orderMapper.toOrderResponse(saved);
    }

    private Long extractUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.parseLong(jwt.getSubject());
    }
}
