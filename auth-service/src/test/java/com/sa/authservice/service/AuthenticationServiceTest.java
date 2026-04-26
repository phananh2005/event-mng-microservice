package com.sa.authservice.service;

import com.nimbusds.jose.JOSEException;
import com.sa.authservice.dto.request.AuthenticationRequest;
import com.sa.authservice.dto.request.IntrospectRequest;
import com.sa.authservice.dto.request.LogoutRequest;
import com.sa.authservice.dto.request.UserCreateRequest;
import com.sa.authservice.dto.response.AuthenticationResponse;
import com.sa.authservice.exception.AppException;
import com.sa.authservice.model.entity.Role;
import com.sa.authservice.model.entity.User;
import com.sa.authservice.repository.InvalidatedTokenRepository;
import com.sa.authservice.repository.RoleRepository;
import com.sa.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private InvalidatedTokenRepository invalidatedTokenRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User enabledUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authenticationService, "signerKey", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(authenticationService, "validDuration", 3600L);
        ReflectionTestUtils.setField(authenticationService, "refreshableDuration", 7200L);

        enabledUser = User.builder()
                .id(1L)
                .username("customer1")
                .password("encoded")
                .enabled(true)
                .roles(List.of(Role.builder().name(com.sa.authservice.model.enums.Role.CUSTOMER).build()))
                .build();
    }

    @Test
    void authenticate_ShouldReturnToken_WhenCredentialsAreValid() {
        when(userRepository.findByUsername("customer1")).thenReturn(Optional.of(enabledUser));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);

        AuthenticationResponse response = authenticationService.authenticate(
                AuthenticationRequest.builder().username("customer1").password("secret").build());

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertFalse(response.getToken().isBlank());
    }

    @Test
    void authenticate_ShouldThrow_WhenPasswordIsInvalid() {
        when(userRepository.findByUsername("customer1")).thenReturn(Optional.of(enabledUser));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(AppException.class, () -> authenticationService.authenticate(
                AuthenticationRequest.builder().username("customer1").password("wrong").build()));
    }

    @Test
    void introspect_ShouldReturnValidTrue_ForFreshToken() throws JOSEException, ParseException {
        when(userRepository.findByUsername("customer1")).thenReturn(Optional.of(enabledUser));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);
        when(invalidatedTokenRepository.existsById(anyString())).thenReturn(false);

        AuthenticationResponse login = authenticationService.authenticate(
                AuthenticationRequest.builder().username("customer1").password("secret").build());

        var introspect = authenticationService.introspect(IntrospectRequest.builder().token(login.getToken()).build());

        assertTrue(introspect.isValid());
    }

    @Test
    void logout_ShouldPersistInvalidatedToken_ForValidToken() throws JOSEException, ParseException {
        when(userRepository.findByUsername("customer1")).thenReturn(Optional.of(enabledUser));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);
        when(invalidatedTokenRepository.existsById(anyString())).thenReturn(false);

        AuthenticationResponse login = authenticationService.authenticate(
                AuthenticationRequest.builder().username("customer1").password("secret").build());

        assertDoesNotThrow(() -> authenticationService.logout(LogoutRequest.builder().token(login.getToken()).build()));
        verify(invalidatedTokenRepository).save(any());
    }

    @Test
    void logout_ShouldNotPersist_WhenTokenInvalid() throws JOSEException, ParseException {
        assertThrows(ParseException.class,
                () -> authenticationService.logout(LogoutRequest.builder().token("bad-token").build()));
        verify(invalidatedTokenRepository, never()).save(any());
    }

    @Test
    void register_ShouldCreateDisabledUserAndSendVerificationMail() {
        UserCreateRequest request = UserCreateRequest.builder()
                .username("new-user")
                .password("secret123")
                .email("new-user@example.com")
                .fullName("New User")
                .build();

        Role customerRole = Role.builder().id(2L).name(com.sa.authservice.model.enums.Role.CUSTOMER).build();

        when(userRepository.findByUsername("new-user")).thenReturn(Optional.empty());
        when(userRepository.existsByEmailAndEnabledTrue("new-user@example.com")).thenReturn(false);
        when(roleRepository.findByName(com.sa.authservice.model.enums.Role.CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-secret");

        String result = authenticationService.register(request);

        assertEquals("Please check your email to verify your account", result);
        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(eq("new-user@example.com"), anyString());
    }

    @Test
    void verifyEmail_ShouldEnableUser_WhenTokenExists() {
        User disabledUser = User.builder()
                .id(2L)
                .username("pending-user")
                .email("pending@example.com")
                .password("encoded")
                .enabled(false)
                .verificationToken("verify-token")
                .roles(List.of(Role.builder().name(com.sa.authservice.model.enums.Role.CUSTOMER).build()))
                .build();

        when(userRepository.findByVerificationToken("verify-token")).thenReturn(Optional.of(disabledUser));

        String result = authenticationService.verifyEmail("verify-token");

        assertEquals("Email verified successfully", result);
        assertTrue(disabledUser.isEnabled());
        assertNull(disabledUser.getVerificationToken());
        verify(userRepository).save(disabledUser);
    }
}


