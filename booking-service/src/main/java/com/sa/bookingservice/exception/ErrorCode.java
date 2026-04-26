package com.sa.bookingservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED(4007, "You do not have permission", HttpStatus.FORBIDDEN),

    CART_EMPTY(6001, "Cart is empty", HttpStatus.BAD_REQUEST),
    CART_ITEM_NOT_FOUND(6002, "Cart item not found", HttpStatus.NOT_FOUND),
    TICKET_TYPE_NOT_FOUND(6003, "Ticket type not found", HttpStatus.NOT_FOUND),
    TICKET_NOT_ENOUGH(6004, "Not enough tickets available", HttpStatus.CONFLICT),
    ORDER_NOT_FOUND(6005, "Order not found", HttpStatus.NOT_FOUND),
    TICKET_SERVICE_ERROR(6006, "Ticket service error", HttpStatus.BAD_GATEWAY);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
