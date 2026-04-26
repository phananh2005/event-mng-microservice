package com.sa.bookingservice.controller;

import com.sa.bookingservice.dto.response.ApiResponse;
import com.sa.bookingservice.dto.response.OrderResponse;
import com.sa.bookingservice.model.enums.PaymentMethod;
import com.sa.bookingservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Booking", description = "Order/checkout APIs")
public class OrderController {

    OrderService orderService;

    @PostMapping("/checkout")
    @Operation(summary = "Checkout full cart")
    public ApiResponse<OrderResponse> checkout(@RequestParam PaymentMethod paymentMethod) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.checkout(paymentMethod)).build();
    }

    @PostMapping("/checkout-selected")
    @Operation(summary = "Checkout selected cart items")
    public ApiResponse<OrderResponse> checkoutSelected(@RequestBody List<Long> itemIds,
                                                       @RequestParam PaymentMethod paymentMethod) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.checkoutSelected(itemIds, paymentMethod)).build();
    }

    @GetMapping
    @Operation(summary = "Get my order history")
    public ApiResponse<Page<OrderResponse>> getMyOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        return ApiResponse.<Page<OrderResponse>>builder()
                .result(orderService.getMyOrders(pageRequest)).build();
    }
}
