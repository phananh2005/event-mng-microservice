package com.sa.checkinservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {
    private Long id;
    private Long orderId;
    private String ticketTypeName;
    private String ticketCode;
    private String status;
    private Long eventId;
}
