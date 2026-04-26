package com.sa.adminservice.model.projection;

import java.math.BigDecimal;

public interface EventRevenueStatsOrganizerProjection {
    String getEventName();
    BigDecimal getTotalRevenue();
    Integer getTicketsSold();
    Double getPercentageOfTicketsSold();
}
