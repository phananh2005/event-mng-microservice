package com.sa.ticketservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED(4007, "You do not have permission", HttpStatus.FORBIDDEN),

    TICKET_TYPE_NOT_FOUND(5001, "Ticket type not found", HttpStatus.NOT_FOUND),
    TICKET_NOT_ENOUGH(5002, "Not enough tickets available", HttpStatus.CONFLICT),
    TICKET_NOT_FOUND(5003, "Ticket not found", HttpStatus.NOT_FOUND),
    TICKET_INVALID(5004, "Ticket is invalid", HttpStatus.BAD_REQUEST),
    TICKET_USED(5005, "Ticket has already been used", HttpStatus.CONFLICT);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
