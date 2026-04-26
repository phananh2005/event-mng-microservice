package com.sa.eventservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    KEY_INVALID(1001, "Uncategorize error", HttpStatus.BAD_REQUEST),
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorize exception", HttpStatus.INTERNAL_SERVER_ERROR),

    UNAUTHORIZED(4007, "You do not have permission", HttpStatus.FORBIDDEN),

    CATEGORY_NOT_FOUND(2001, "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_NAME_REQUIRED(2003, "Category name is required", HttpStatus.BAD_REQUEST),
    CATEGORY_NAME_INVALID(2004, "Category name must be at least 5 characters", HttpStatus.BAD_REQUEST),

    EVENT_NOT_FOUND(3001, "Event not found", HttpStatus.NOT_FOUND),
    EVENT_NAME_REQUIRED(3002, "Event name is required", HttpStatus.BAD_REQUEST),
    CATEGORY_ID_REQUIRED(3003, "Category id is required", HttpStatus.BAD_REQUEST),
    START_TIME_REQUIRED(3004, "Start time is required", HttpStatus.BAD_REQUEST),
    END_TIME_REQUIRED(3005, "End time is required", HttpStatus.BAD_REQUEST),
    EVENT_FORBIDDEN(3006, "You are not the owner of this event", HttpStatus.FORBIDDEN);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
