package com.sa.authservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    KEY_INVALID(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),

    USER_NOT_EXISTED(1003, "User not existed", HttpStatus.NOT_FOUND),
    USERNAME_EXISTED(1021, "Username already existed", HttpStatus.BAD_REQUEST),
    USER_DISABLED(1016, "Account is disabled", HttpStatus.FORBIDDEN),
    PASSWORD_NOT_MATCH(1018, "Password does not match", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1004, "Username must be at least 3 characters", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1005, "Password must be at least 6 characters", HttpStatus.BAD_REQUEST),
    USERNAME_REQUIRED(1006, "Username is required", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED(1007, "Password is required", HttpStatus.BAD_REQUEST),
    EMAIL_REQUIRED(1008, "Email is required", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(1009, "Email format is invalid", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1010, "Email already existed", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_VERIFIED(1022, "The email has already been sent. Please check your inbox again.", HttpStatus.CONFLICT),
    FULLNAME_REQUIRED(1011, "Full name is required", HttpStatus.BAD_REQUEST),
    FULLNAME_TOO_LONG(1012, "Full name is too long", HttpStatus.BAD_REQUEST),
    PHONE_INVALID(1013, "Phone number is invalid", HttpStatus.BAD_REQUEST),
    ADDRESS_TOO_LONG(1014, "Address is too long", HttpStatus.BAD_REQUEST),

    INVALID_TOKEN(1020, "Invalid token", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(4006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(4007, "You do not have permission", HttpStatus.FORBIDDEN);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}

