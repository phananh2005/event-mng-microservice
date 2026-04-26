package com.sa.ticketservice.controller;

import com.sa.ticketservice.dto.request.IssueTicketRequest;
import com.sa.ticketservice.dto.response.ApiResponse;
import com.sa.ticketservice.dto.response.TicketResponse;
import com.sa.ticketservice.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Ticket", description = "Ticket APIs")
public class TicketController {

    TicketService ticketService;

    @GetMapping("/my-tickets")
    @Operation(summary = "Get my purchased tickets")
    public ApiResponse<List<TicketResponse>> getMyTickets() {
        return ApiResponse.<List<TicketResponse>>builder()
                .result(ticketService.getMyTickets())
                .build();
    }

    @PostMapping("/check-in")
    @Operation(summary = "Check-in ticket by code (ADMIN/ORGANIZER)")
    public ApiResponse<TicketResponse> checkIn(@RequestParam String ticketCode) {
        return ApiResponse.<TicketResponse>builder()
                .result(ticketService.checkIn(ticketCode))
                .build();
    }

    @PostMapping("/internal/issue")
    @Operation(summary = "Issue tickets after payment — called by booking-service")
    public ApiResponse<List<TicketResponse>> issue(@RequestBody @Valid IssueTicketRequest request) {
        return ApiResponse.<List<TicketResponse>>builder()
                .result(ticketService.issue(request))
                .build();
    }
}
