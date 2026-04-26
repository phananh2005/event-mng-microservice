package com.sa.adminservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventTemporalStatsResponse {
    private String day;
    private List<EventTemporalStatsDetail> eventTemporalStatsDetail;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EventTemporalStatsDetail {
        private Integer hourOfDay;
        private Long countEvents;
        private Long totalTickets;
        private Long ticketsSold;
        private Double percentageOfTicketsSold;
    }
}
