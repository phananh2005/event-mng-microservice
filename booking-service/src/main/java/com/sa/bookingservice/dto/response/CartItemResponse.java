package com.sa.bookingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    private Long id;
    private Long ticketTypeId;
    private String ticketTypeName;
    private String eventName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
