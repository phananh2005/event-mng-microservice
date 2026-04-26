package com.sa.adminservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRevenueStatsAdminResponse {
    private BigDecimal totalRevenue;
    private List<MonthlyRevenueResponse> monthlyRevenues;
}
