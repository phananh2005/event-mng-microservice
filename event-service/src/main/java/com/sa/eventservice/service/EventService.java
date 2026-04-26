package com.sa.eventservice.service;

import com.sa.eventservice.dto.request.EventRequest;
import com.sa.eventservice.dto.response.EventResponse;
import com.sa.eventservice.exception.AppException;
import com.sa.eventservice.exception.ErrorCode;
import com.sa.eventservice.mapper.EventMapper;
import com.sa.eventservice.model.entity.Category;
import com.sa.eventservice.model.entity.Event;
import com.sa.eventservice.model.entity.EventImage;
import com.sa.eventservice.model.entity.TicketType;
import com.sa.eventservice.model.enums.EventStatus;
import com.sa.eventservice.repository.CategoryRepository;
import com.sa.eventservice.repository.EventRepository;
import com.sa.eventservice.repository.TicketTypeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventService {

    EventRepository eventRepository;
    CategoryRepository categoryRepository;
    TicketTypeRepository ticketTypeRepository;
    EventMapper eventMapper;

    @NonFinal
    @Value("${app.file.base-url:http://localhost:8082/uploads}")
    String fileBaseUrl;

    public Page<EventResponse> getAllPublished(PageRequest pageRequest) {
        List<EventStatus> activeStatuses = List.of(
                EventStatus.UPCOMING,
                EventStatus.OPENING,
                EventStatus.CLOSED
        );
        return eventRepository.findByStatusIn(activeStatuses, pageRequest)
                .map(eventMapper::toEventResponse);
    }

    public EventResponse getById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return eventMapper.toEventResponse(event);
    }

    @Transactional
    @PreAuthorize("hasRole('ORGANIZER')")
    public EventResponse create(EventRequest request) {
        Long organizerId = extractUserId();
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        Event event = Event.builder()
                .name(request.getName())
                .category(category)
                .organizerId(organizerId)
                .location(request.getLocation())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .saleStartDate(request.getSaleStartDate())
                .saleEndDate(request.getSaleEndDate())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : EventStatus.PENDING)
                .build();

        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            event.setImages(saveImages(request.getFiles(), event));
        }

        Event saved = eventRepository.save(event);

        if (request.getTicketTypes() != null) {
            request.getTicketTypes().forEach(tt -> ticketTypeRepository.save(
                    TicketType.builder()
                            .event(saved)
                            .name(tt.getName())
                            .price(tt.getPrice())
                            .totalQuantity(tt.getTotalQuantity())
                            .remainingQuantity(tt.getTotalQuantity())
                            .description(tt.getDescription())
                            .build()
            ));
        }

        return eventMapper.toEventResponse(saved);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public EventResponse update(Long id, EventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        if (!isAdmin()) {
            Long callerId = extractUserId();
            if (!callerId.equals(event.getOrganizerId())) {
                throw new AppException(ErrorCode.EVENT_FORBIDDEN);
            }
        }

        eventMapper.updateEvent(request, event);

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            event.setCategory(category);
        }

        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            event.getImages().addAll(saveImages(request.getFiles(), event));
        }

        return eventMapper.toEventResponse(eventRepository.save(event));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public EventResponse updateStatus(Long id, EventStatus status) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        event.setStatus(status);
        return eventMapper.toEventResponse(eventRepository.save(event));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<EventResponse> getAllForAdmin(String search, String status, PageRequest pageRequest) {
        boolean hasSearch = search != null && !search.isBlank();
        boolean hasStatus = status != null && !status.isBlank();

        Page<Event> events;
        if (hasSearch && hasStatus) {
            events = eventRepository.findByNameContainingIgnoreCaseAndStatus(
                    search, EventStatus.valueOf(status), pageRequest);
        } else if (hasSearch) {
            events = eventRepository.findByNameContainingIgnoreCase(search, pageRequest);
        } else if (hasStatus) {
            events = eventRepository.findByStatus(EventStatus.valueOf(status), pageRequest);
        } else {
            events = eventRepository.findAll(pageRequest);
        }
        return events.map(eventMapper::toEventResponse);
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    public Page<EventResponse> getMyEvents(PageRequest pageRequest) {
        Long organizerId = extractUserId();
        return eventRepository.findByOrganizerId(organizerId, pageRequest)
                .map(eventMapper::toEventResponse);
    }

    // --- helpers ---

    private Long extractUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.parseLong(jwt.getSubject());
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private List<EventImage> saveImages(List<MultipartFile> files, Event event) {
        List<EventImage> images = new ArrayList<>();
        File uploadDir = new File("uploads/");
        if (!uploadDir.exists()) uploadDir.mkdirs();

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            try {
                String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                file.transferTo(new File(uploadDir.getAbsolutePath() + File.separator + filename));
                images.add(EventImage.builder().imageUrl(fileBaseUrl + "/" + filename).event(event).build());
            } catch (IOException e) {
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
        }
        return images;
    }
}
