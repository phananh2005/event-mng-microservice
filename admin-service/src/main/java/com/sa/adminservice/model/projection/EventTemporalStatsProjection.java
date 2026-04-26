package com.sa.adminservice.model.projection;

public interface EventTemporalStatsProjection {
    Integer getHourOfDay();
    Long getCountEvents();
    Long getTotalTickets();
    Long getTicketsSold();
    Double getPercentageOfTicketsSold();
}
