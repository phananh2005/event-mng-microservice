package com.sa.bookingservice.faker;

import com.sa.bookingservice.model.entity.Order;
import com.sa.bookingservice.model.entity.OrderItem;
import com.sa.bookingservice.model.enums.OrderStatus;
import com.sa.bookingservice.model.enums.PaymentMethod;
import com.sa.bookingservice.model.enums.PaymentStatus;
import com.sa.bookingservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderSeeder {

    private final OrderRepository orderRepository;

    public void seed() {
        if (orderRepository.count() > 0) return;

        Order order1 = Order.builder()
                .customerId(3L)
                .totalAmount(BigDecimal.valueOf(300000))
                .serviceFee(BigDecimal.valueOf(15000))
                .organizerAmount(BigDecimal.valueOf(285000))
                .platformFeeRate(0.05f)
                .paymentMethod(PaymentMethod.VNPAY)
                .paymentStatus(PaymentStatus.PAID)
                .orderStatus(OrderStatus.CONFIRMED)
                .orderDate(LocalDateTime.now().minusDays(3))
                .paidAt(LocalDateTime.now().minusDays(3))
                .build();

        OrderItem item1 = OrderItem.builder()
                .order(order1)
                .ticketTypeId(1L)
                .ticketTypeName("General Admission")
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(150000))
                .subtotal(BigDecimal.valueOf(300000))
                .build();

        order1.setItems(List.of(item1));

        Order order2 = Order.builder()
                .customerId(3L)
                .totalAmount(BigDecimal.valueOf(500000))
                .serviceFee(BigDecimal.valueOf(25000))
                .organizerAmount(BigDecimal.valueOf(475000))
                .platformFeeRate(0.05f)
                .paymentMethod(PaymentMethod.MOMO)
                .paymentStatus(PaymentStatus.PAID)
                .orderStatus(OrderStatus.CONFIRMED)
                .orderDate(LocalDateTime.now().minusDays(1))
                .paidAt(LocalDateTime.now().minusDays(1))
                .build();

        OrderItem item2 = OrderItem.builder()
                .order(order2)
                .ticketTypeId(2L)
                .ticketTypeName("VIP")
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(500000))
                .subtotal(BigDecimal.valueOf(500000))
                .build();

        order2.setItems(List.of(item2));

        orderRepository.saveAll(List.of(order1, order2));
    }
}
