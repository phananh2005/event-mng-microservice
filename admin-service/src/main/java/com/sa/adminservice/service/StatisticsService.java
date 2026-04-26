package com.sa.adminservice.service;

import com.sa.adminservice.dto.response.*;
import com.sa.adminservice.model.projection.*;
import com.sa.adminservice.repository.StatisticsRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatisticsService {

    StatisticsRepository statisticsRepository;

    private static final Map<Integer, String> DAY_OF_WEEK = Map.of(
            1, "Sunday", 2, "Monday", 3, "Tuesday", 4, "Wednesday",
            5, "Thursday", 6, "Friday", 7, "Saturday");

    @PreAuthorize("hasRole('ADMIN')")
    public EventStatusStatsResponse getEventStatusStats(Long quarter, Long year) {
        List<EventStatusStatsProjection> rows = statisticsRepository.findEventStatusStats(quarter, year);
        long total = rows.stream().mapToLong(EventStatusStatsProjection::getCount).sum();

        List<EventStatusStatsResponse.EventStatusStatsDetail> details = rows.stream()
                .map(r -> new EventStatusStatsResponse.EventStatusStatsDetail(
                        r.getStatus(),
                        total == 0 ? 0.0 : r.getCount() * 100.0 / total,
                        r.getCount()))
                .toList();

        return EventStatusStatsResponse.builder()
                .quarter(quarter).year(year).total(total)
                .eventStatusStatsDetail(details)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public EventTemporalStatsResponse getEventTemporalStats(int dayOfWeek) {
        List<EventTemporalStatsProjection> rows = statisticsRepository.findEventTemporalStats(dayOfWeek);

        List<EventTemporalStatsResponse.EventTemporalStatsDetail> details = rows.stream()
                .map(r -> new EventTemporalStatsResponse.EventTemporalStatsDetail(
                        r.getHourOfDay(), r.getCountEvents(), r.getTotalTickets(),
                        r.getTicketsSold(), r.getPercentageOfTicketsSold()))
                .toList();

        return EventTemporalStatsResponse.builder()
                .day(DAY_OF_WEEK.getOrDefault(dayOfWeek, "Unknown"))
                .eventTemporalStatsDetail(details)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<EventRevenueStatsOrganizerResponse> getEventRevenueStatsOrganizer(Long organizerId) {
        return statisticsRepository.findEventRevenueOrganizerStats(organizerId).stream()
                .map(r -> EventRevenueStatsOrganizerResponse.builder()
                        .eventName(r.getEventName())
                        .totalRevenue(r.getTotalRevenue())
                        .ticketsSold(r.getTicketsSold())
                        .percentageOfTicketsSold(r.getPercentageOfTicketsSold())
                        .build())
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public EventRevenueStatsAdminResponse getEventRevenueStatsAdmin() {
        EventRevenueStatsAdminProjection total = statisticsRepository.findEventRevenueAdminStats();
        List<MonthlyRevenueProjection> monthly = statisticsRepository.findMonthlyRevenueAdmin();

        List<MonthlyRevenueResponse> monthlyList = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate d = now.minusMonths(i);
            int y = d.getYear(), m = d.getMonthValue();
            BigDecimal rev = monthly.stream()
                    .filter(p -> p.getYear() == y && p.getMonth() == m)
                    .map(MonthlyRevenueProjection::getRevenue)
                    .findFirst().orElse(BigDecimal.ZERO);
            monthlyList.add(MonthlyRevenueResponse.builder().year(y).month(m).revenue(rev).build());
        }

        return EventRevenueStatsAdminResponse.builder()
                .totalRevenue(total != null && total.getTotalRevenue() != null
                        ? total.getTotalRevenue() : BigDecimal.ZERO)
                .monthlyRevenues(monthlyList)
                .build();
    }
}
