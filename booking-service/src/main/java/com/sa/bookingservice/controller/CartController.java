package com.sa.bookingservice.controller;

import com.sa.bookingservice.dto.request.CartItemRequest;
import com.sa.bookingservice.dto.response.ApiResponse;
import com.sa.bookingservice.dto.response.CartResponse;
import com.sa.bookingservice.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Cart", description = "Cart APIs")
public class CartController {

    CartService cartService;

    @PostMapping("/add")
    @Operation(summary = "Add ticket to cart")
    public ApiResponse<CartResponse> addToCart(@RequestBody @Valid CartItemRequest request) {
        return ApiResponse.<CartResponse>builder().result(cartService.addToCart(request)).build();
    }

    @GetMapping
    @Operation(summary = "Get my cart")
    public ApiResponse<CartResponse> getMyCart() {
        return ApiResponse.<CartResponse>builder().result(cartService.getMyCart()).build();
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update item quantity")
    public ApiResponse<CartResponse> updateQuantity(@PathVariable Long itemId,
                                                    @RequestParam Integer quantity) {
        return ApiResponse.<CartResponse>builder()
                .result(cartService.updateQuantity(itemId, quantity)).build();
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart")
    public ApiResponse<CartResponse> removeItem(@PathVariable Long itemId) {
        return ApiResponse.<CartResponse>builder().result(cartService.removeItem(itemId)).build();
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear cart")
    public ApiResponse<Void> clearCart() {
        cartService.clearCart();
        return ApiResponse.<Void>builder().build();
    }
}
