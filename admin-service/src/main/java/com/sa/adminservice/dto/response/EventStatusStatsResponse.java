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
public class EventStatusStatsResponse {
    private Long quarter;
    private Long year;
    private Long total;
    private List<EventStatusStatsDetail> eventStatusStatsDetail;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EventStatusStatsDetail {
        private String status;
        private Double percentage;
        private Long countEvents;
    }
}
