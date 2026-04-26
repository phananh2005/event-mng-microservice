package com.sa.adminservice.controller;

import com.sa.adminservice.dto.response.*;
import com.sa.adminservice.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/statistics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Admin Statistics", description = "Read-only dashboard statistics (ADMIN only)")
public class StatisticsController {

    StatisticsService statisticsService;

    @GetMapping("/by-status/{quarter}/{year}")
    @Operation(summary = "Event status distribution by quarter/year")
    public ApiResponse<EventStatusStatsResponse> getByStatus(
            @PathVariable Long quarter, @PathVariable Long year) {
        return ApiResponse.<EventStatusStatsResponse>builder()
                .result(statisticsService.getEventStatusStats(quarter, year)).build();
    }

    @GetMapping("/by-temporal/{dayOfWeek}")
    @Operation(summary = "Event count by hour for a given day of week (1=Sun … 7=Sat)")
    public ApiResponse<EventTemporalStatsResponse> getByTemporal(@PathVariable int dayOfWeek) {
        return ApiResponse.<EventTemporalStatsResponse>builder()
                .result(statisticsService.getEventTemporalStats(dayOfWeek)).build();
    }

    @GetMapping("/revenue/organizer/{organizerId}")
    @Operation(summary = "Revenue stats per event for an organizer")
    public ApiResponse<List<EventRevenueStatsOrganizerResponse>> getRevenueOrganizer(
            @PathVariable Long organizerId) {
        return ApiResponse.<List<EventRevenueStatsOrganizerResponse>>builder()
                .result(statisticsService.getEventRevenueStatsOrganizer(organizerId)).build();
    }

    @GetMapping("/revenue/admin")
    @Operation(summary = "Platform-wide revenue stats (service fees)")
    public ApiResponse<EventRevenueStatsAdminResponse> getRevenueAdmin() {
        return ApiResponse.<EventRevenueStatsAdminResponse>builder()
                .result(statisticsService.getEventRevenueStatsAdmin()).build();
    }
}
