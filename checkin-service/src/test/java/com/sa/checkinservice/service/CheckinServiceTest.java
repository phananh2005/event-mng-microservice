package com.sa.checkinservice.service;

import com.sa.checkinservice.client.TicketServiceClient;
import com.sa.checkinservice.dto.request.ScanRequest;
import com.sa.checkinservice.dto.response.ApiResponse;
import com.sa.checkinservice.dto.response.CheckinLogResponse;
import com.sa.checkinservice.dto.response.TicketResponse;
import com.sa.checkinservice.mapper.CheckinLogMapper;
import com.sa.checkinservice.model.entity.CheckinLog;
import com.sa.checkinservice.model.enums.CheckinResult;
import com.sa.checkinservice.repository.CheckinLogRepository;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckinServiceTest {

    @Mock CheckinLogRepository checkinLogRepository;
    @Mock TicketServiceClient ticketServiceClient;
    @Mock CheckinLogMapper checkinLogMapper;
    @InjectMocks CheckinService checkinService;

    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    private void setAuth(Long userId) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS512").subject(userId.toString())
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(3600))
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(jwt, null, List.of()));
    }

    private FeignException feignException(int status) {
        Request req = Request.create(Request.HttpMethod.POST, "/", Map.of(), null,
                StandardCharsets.UTF_8, null);
        return FeignException.errorStatus("checkIn", feign.Response.builder()
                .status(status).reason("err").request(req).headers(Map.of()).build());
    }

    private ScanRequest scanRequest() {
        return ScanRequest.builder().ticketCode("TKT-ABC123").eventId(10L).build();
    }

    private void stubSave(CheckinResult result) {
        CheckinLog saved = CheckinLog.builder().id(1L).result(result).build();
        when(checkinLogRepository.save(any())).thenReturn(saved);
        when(checkinLogMapper.toResponse(saved))
                .thenReturn(CheckinLogResponse.builder().result(result).build());
    }

    @Test
    void scan_shouldLogSuccessWhenTicketServiceReturnsOk() {
        setAuth(1L);
        when(ticketServiceClient.checkIn("TKT-ABC123"))
                .thenReturn(ApiResponse.<TicketResponse>builder().build());
        stubSave(CheckinResult.SUCCESS);

        CheckinLogResponse result = checkinService.scan(scanRequest());

        assertEquals(CheckinResult.SUCCESS, result.getResult());
        ArgumentCaptor<CheckinLog> captor = ArgumentCaptor.forClass(CheckinLog.class);
        verify(checkinLogRepository).save(captor.capture());
        assertEquals(CheckinResult.SUCCESS, captor.getValue().getResult());
    }

    @Test
    void scan_shouldLogAlreadyUsedOnConflict() {
        setAuth(1L);
        when(ticketServiceClient.checkIn("TKT-ABC123")).thenThrow(feignException(409));
        stubSave(CheckinResult.ALREADY_USED);

        CheckinLogResponse result = checkinService.scan(scanRequest());

        assertEquals(CheckinResult.ALREADY_USED, result.getResult());
        ArgumentCaptor<CheckinLog> captor = ArgumentCaptor.forClass(CheckinLog.class);
        verify(checkinLogRepository).save(captor.capture());
        assertEquals(CheckinResult.ALREADY_USED, captor.getValue().getResult());
    }

    @Test
    void scan_shouldLogNotFoundOnFeignNotFound() {
        setAuth(1L);
        when(ticketServiceClient.checkIn("TKT-ABC123")).thenThrow(feignException(404));
        stubSave(CheckinResult.NOT_FOUND);

        CheckinLogResponse result = checkinService.scan(scanRequest());

        assertEquals(CheckinResult.NOT_FOUND, result.getResult());
    }

    @Test
    void scan_shouldAlwaysPersistLogRegardlessOfOutcome() {
        setAuth(1L);
        when(ticketServiceClient.checkIn(any())).thenThrow(new RuntimeException("network error"));
        stubSave(CheckinResult.INVALID);

        checkinService.scan(scanRequest());

        verify(checkinLogRepository, times(1)).save(any());
    }
}
