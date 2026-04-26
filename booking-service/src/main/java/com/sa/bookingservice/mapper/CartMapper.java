package com.sa.bookingservice.mapper;

import com.sa.bookingservice.dto.response.CartItemResponse;
import com.sa.bookingservice.dto.response.CartResponse;
import com.sa.bookingservice.model.entity.Cart;
import com.sa.bookingservice.model.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

    CartItemResponse toItemResponse(CartItem item);

    default CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems() == null ? List.of()
                : cart.getItems().stream().map(this::toItemResponse).toList();
        BigDecimal total = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return CartResponse.builder()
                .id(cart.getId())
                .items(items)
                .totalAmount(total)
                .build();
    }
}
