package com.sa.ticketservice.dto.response;

import com.sa.ticketservice.model.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {
    private Long id;
    private Long orderId;
    private String ticketTypeName;
    private String eventName;
    private String ticketCode;
    private String qrCode;
    private TicketStatus status;
    private LocalDateTime usedAt;
}
