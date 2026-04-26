package com.sa.eventservice.controller;

import com.sa.eventservice.dto.response.ApiResponse;
import com.sa.eventservice.dto.response.EventResponse;
import com.sa.eventservice.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Event", description = "Read event APIs migrated from monolith")
public class EventController {

    EventService eventService;

    @GetMapping
    @Operation(summary = "Get published events")
    public ApiResponse<Page<EventResponse>> getAllPublished(@RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        return ApiResponse.<Page<EventResponse>>builder()
                .result(eventService.getAllPublished(pageRequest))
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event details")
    public ApiResponse<EventResponse> getById(@PathVariable Long id) {
        return ApiResponse.<EventResponse>builder()
                .result(eventService.getById(id))
                .build();
    }
}

