package com.sa.ticketservice.controller;

import com.sa.ticketservice.dto.request.ReserveRequest;
import com.sa.ticketservice.dto.response.ApiResponse;
import com.sa.ticketservice.dto.response.TicketTypeResponse;
import com.sa.ticketservice.service.TicketTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ticket-types")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "TicketType", description = "Ticket type APIs")
public class TicketTypeController {

    TicketTypeService ticketTypeService;

    @GetMapping("/event/{eventId}")
    @Operation(summary = "Get ticket types by event")
    public ApiResponse<List<TicketTypeResponse>> getByEvent(@PathVariable Long eventId) {
        return ApiResponse.<List<TicketTypeResponse>>builder()
                .result(ticketTypeService.getByEvent(eventId))
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ticket type by id")
    public ApiResponse<TicketTypeResponse> getById(@PathVariable Long id) {
        return ApiResponse.<TicketTypeResponse>builder()
                .result(ticketTypeService.getById(id))
                .build();
    }

    @PostMapping("/internal/reserve")
    @Operation(summary = "Reserve (deduct) stock — called by booking-service")
    public ApiResponse<Void> reserve(@RequestBody @Valid ReserveRequest request) {
        ticketTypeService.reserve(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/internal/release")
    @Operation(summary = "Release (restore) stock — called on payment failure")
    public ApiResponse<Void> release(@RequestBody @Valid ReserveRequest request) {
        ticketTypeService.release(request);
        return ApiResponse.<Void>builder().build();
    }
}
