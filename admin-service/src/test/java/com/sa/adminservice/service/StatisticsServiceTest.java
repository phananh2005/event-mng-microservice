package com.sa.adminservice.service;

import com.sa.adminservice.dto.response.EventRevenueStatsAdminResponse;
import com.sa.adminservice.dto.response.EventStatusStatsResponse;
import com.sa.adminservice.dto.response.EventTemporalStatsResponse;
import com.sa.adminservice.model.projection.*;
import com.sa.adminservice.repository.StatisticsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock StatisticsRepository statisticsRepository;
    @InjectMocks StatisticsService statisticsService;

    @Test
    void getEventStatusStats_shouldCalculatePercentagesCorrectly() {
        EventStatusStatsProjection p1 = mockStatusProjection("UPCOMING", 3L);
        EventStatusStatsProjection p2 = mockStatusProjection("OPENING", 1L);
        when(statisticsRepository.findEventStatusStats(1L, 2024L))
                .thenReturn(List.of(p1, p2));

        EventStatusStatsResponse result = statisticsService.getEventStatusStats(1L, 2024L);

        assertEquals(4L, result.getTotal());
        assertEquals(2, result.getEventStatusStatsDetail().size());
        double upcomingPct = result.getEventStatusStatsDetail().stream()
                .filter(d -> "UPCOMING".equals(d.getStatus()))
                .mapToDouble(EventStatusStatsResponse.EventStatusStatsDetail::getPercentage)
                .findFirst().orElse(0);
        assertEquals(75.0, upcomingPct, 0.01);
    }

    @Test
    void getEventStatusStats_shouldReturnZeroPercentageWhenNoEvents() {
        when(statisticsRepository.findEventStatusStats(2L, 2024L)).thenReturn(List.of());

        EventStatusStatsResponse result = statisticsService.getEventStatusStats(2L, 2024L);

        assertEquals(0L, result.getTotal());
        assertTrue(result.getEventStatusStatsDetail().isEmpty());
    }

    @Test
    void getEventTemporalStats_shouldMapDayOfWeekName() {
        EventTemporalStatsProjection row = mockTemporalProjection(9, 5L, 100L, 80L, 80.0);
        when(statisticsRepository.findEventTemporalStats(2)).thenReturn(List.of(row));

        EventTemporalStatsResponse result = statisticsService.getEventTemporalStats(2);

        assertEquals("Monday", result.getDay());
        assertEquals(1, result.getEventTemporalStatsDetail().size());
        assertEquals(9, result.getEventTemporalStatsDetail().get(0).getHourOfDay());
    }

    @Test
    void getEventRevenueStatsAdmin_shouldReturnZeroWhenNoOrders() {
        when(statisticsRepository.findEventRevenueAdminStats()).thenReturn(null);
        when(statisticsRepository.findMonthlyRevenueAdmin()).thenReturn(List.of());

        EventRevenueStatsAdminResponse result = statisticsService.getEventRevenueStatsAdmin();

        assertEquals(BigDecimal.ZERO, result.getTotalRevenue());
        assertEquals(6, result.getMonthlyRevenues().size()); // last 6 months always present
    }

    @Test
    void getEventRevenueStatsAdmin_shouldAggregateTotalRevenue() {
        EventRevenueStatsAdminProjection proj = () -> BigDecimal.valueOf(50000);
        when(statisticsRepository.findEventRevenueAdminStats()).thenReturn(proj);
        when(statisticsRepository.findMonthlyRevenueAdmin()).thenReturn(List.of());

        EventRevenueStatsAdminResponse result = statisticsService.getEventRevenueStatsAdmin();

        assertEquals(BigDecimal.valueOf(50000), result.getTotalRevenue());
    }

    // --- helpers ---

    private EventStatusStatsProjection mockStatusProjection(String status, Long count) {
        return new EventStatusStatsProjection() {
            public String getStatus() { return status; }
            public Long getCount() { return count; }
        };
    }

    private EventTemporalStatsProjection mockTemporalProjection(int hour, long events,
                                                                 long total, long sold, double pct) {
        return new EventTemporalStatsProjection() {
            public Integer getHourOfDay() { return hour; }
            public Long getCountEvents() { return events; }
            public Long getTotalTickets() { return total; }
            public Long getTicketsSold() { return sold; }
            public Double getPercentageOfTicketsSold() { return pct; }
        };
    }
}
