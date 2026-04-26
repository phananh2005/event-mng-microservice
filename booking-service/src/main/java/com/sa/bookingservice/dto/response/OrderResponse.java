package com.sa.bookingservice.dto.response;

import com.sa.bookingservice.model.enums.OrderStatus;
import com.sa.bookingservice.model.enums.PaymentMethod;
import com.sa.bookingservice.model.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private BigDecimal totalAmount;
    private BigDecimal serviceFee;
    private BigDecimal organizerAmount;
    private Float platformFeeRate;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private OrderStatus orderStatus;
    private LocalDateTime orderDate;
}
