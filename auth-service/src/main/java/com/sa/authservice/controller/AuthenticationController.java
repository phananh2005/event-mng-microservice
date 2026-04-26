package com.sa.authservice.controller;

import com.nimbusds.jose.JOSEException;
import com.sa.authservice.dto.request.AuthenticationRequest;
import com.sa.authservice.dto.request.IntrospectRequest;
import com.sa.authservice.dto.request.LogoutRequest;
import com.sa.authservice.dto.request.RefreshRequest;
import com.sa.authservice.dto.request.UserCreateRequest;
import com.sa.authservice.dto.response.ApiResponse;
import com.sa.authservice.dto.response.AuthenticationResponse;
import com.sa.authservice.dto.response.IntrospectResponse;
import com.sa.authservice.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    AuthenticationService authenticationService;

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody @Valid AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody @Valid UserCreateRequest request) {
        var result = authenticationService.register(request);
        return ApiResponse.<String>builder().result(result).build();
    }

    @GetMapping(value = "/verify", produces = "text/plain;charset=UTF-8")
    public String verifyEmail(@RequestParam("token") String token) {
        return authenticationService.verifyEmail(token);
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
            throws JOSEException, ParseException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request)
            throws JOSEException, ParseException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws JOSEException, ParseException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().build();
    }
}

