package com.sa.checkinservice.client;

import com.sa.checkinservice.dto.response.ApiResponse;
import com.sa.checkinservice.dto.response.TicketResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ticket-service")
public interface TicketServiceClient {

    @PostMapping("/api/v1/tickets/check-in")
    ApiResponse<TicketResponse> checkIn(@RequestParam("ticketCode") String ticketCode);
}
