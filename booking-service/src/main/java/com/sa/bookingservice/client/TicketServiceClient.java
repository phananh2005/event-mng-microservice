package com.sa.bookingservice.client;

import com.sa.bookingservice.dto.response.ApiResponse;
import com.sa.bookingservice.dto.response.TicketTypeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "ticket-service")
public interface TicketServiceClient {

    @GetMapping("/api/v1/ticket-types/{id}")
    ApiResponse<TicketTypeResponse> getTicketType(@PathVariable("id") Long id);

    @PostMapping("/api/v1/ticket-types/internal/reserve")
    ApiResponse<Void> reserve(@RequestBody Map<String, Object> request);

    @PostMapping("/api/v1/ticket-types/internal/release")
    ApiResponse<Void> release(@RequestBody Map<String, Object> request);

    @PostMapping("/api/v1/tickets/internal/issue")
    ApiResponse<Void> issue(@RequestBody Map<String, Object> request);
}
