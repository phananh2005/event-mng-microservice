package com.sa.eventservice.controller;

import com.sa.eventservice.dto.request.EventRequest;
import com.sa.eventservice.dto.response.ApiResponse;
import com.sa.eventservice.dto.response.EventResponse;
import com.sa.eventservice.model.enums.EventStatus;
import com.sa.eventservice.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Event", description = "Event APIs migrated from monolith")
@SecurityRequirement(name = "bearerAuth")
public class EventController {

    EventService eventService;

    @GetMapping
    @Operation(summary = "Get published events")
    public ApiResponse<Page<EventResponse>> getAllPublished(
            @RequestParam(defaultValue = "1") int page,
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create event (ORGANIZER)")
    public ApiResponse<EventResponse> create(@ModelAttribute @Valid EventRequest request) {
        return ApiResponse.<EventResponse>builder()
                .result(eventService.create(request))
                .build();
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update event (ORGANIZER owner / ADMIN)")
    public ApiResponse<EventResponse> update(@PathVariable Long id,
                                             @ModelAttribute @Valid EventRequest request) {
        return ApiResponse.<EventResponse>builder()
                .result(eventService.update(id, request))
                .build();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update event status (ADMIN)")
    public ApiResponse<EventResponse> updateStatus(@PathVariable Long id,
                                                   @RequestParam EventStatus status) {
        return ApiResponse.<EventResponse>builder()
                .result(eventService.updateStatus(id, status))
                .build();
    }

    @GetMapping("/admin/all")
    @Operation(summary = "Get all events with filters (ADMIN)")
    public ApiResponse<Page<EventResponse>> getAllForAdmin(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) String status) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        return ApiResponse.<Page<EventResponse>>builder()
                .result(eventService.getAllForAdmin(search, status, pageRequest))
                .build();
    }

    @GetMapping("/organizer/my-events")
    @Operation(summary = "Get my events (ORGANIZER)")
    public ApiResponse<Page<EventResponse>> getMyEvents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        return ApiResponse.<Page<EventResponse>>builder()
                .result(eventService.getMyEvents(pageRequest))
                .build();
    }
}
