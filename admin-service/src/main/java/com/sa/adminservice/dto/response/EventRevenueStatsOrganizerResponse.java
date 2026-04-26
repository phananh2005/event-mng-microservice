package com.sa.adminservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRevenueStatsOrganizerResponse {
    private String eventName;
    private BigDecimal totalRevenue;
    private Integer ticketsSold;
    private Double percentageOfTicketsSold;
}
