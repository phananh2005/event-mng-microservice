package com.sa.checkinservice.controller;

import com.sa.checkinservice.dto.request.ScanRequest;
import com.sa.checkinservice.dto.response.ApiResponse;
import com.sa.checkinservice.dto.response.CheckinLogResponse;
import com.sa.checkinservice.service.CheckinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/checkin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Checkin", description = "Ticket check-in APIs")
public class CheckinController {

    CheckinService checkinService;

    @PostMapping("/scan")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    @Operation(summary = "Scan ticket QR code to check in")
    public ApiResponse<CheckinLogResponse> scan(@RequestBody @Valid ScanRequest request) {
        return ApiResponse.<CheckinLogResponse>builder()
                .result(checkinService.scan(request))
                .build();
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    @Operation(summary = "Get check-in log for an event")
    public ApiResponse<List<CheckinLogResponse>> getLogsByEvent(@PathVariable Long eventId) {
        return ApiResponse.<List<CheckinLogResponse>>builder()
                .result(checkinService.getLogsByEvent(eventId))
                .build();
    }
}
