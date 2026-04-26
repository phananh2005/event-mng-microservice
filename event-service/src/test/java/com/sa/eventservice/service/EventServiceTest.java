package com.sa.eventservice.service;

import com.sa.eventservice.dto.request.EventRequest;
import com.sa.eventservice.dto.response.EventResponse;
import com.sa.eventservice.exception.AppException;
import com.sa.eventservice.exception.ErrorCode;
import com.sa.eventservice.mapper.EventMapper;
import com.sa.eventservice.model.entity.Category;
import com.sa.eventservice.model.entity.Event;
import com.sa.eventservice.model.enums.EventStatus;
import com.sa.eventservice.repository.CategoryRepository;
import com.sa.eventservice.repository.EventRepository;
import com.sa.eventservice.repository.TicketTypeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock EventRepository eventRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock TicketTypeRepository ticketTypeRepository;
    @Mock EventMapper eventMapper;

    @InjectMocks EventService eventService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void setAuth(Long userId, String... roles) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS512")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claims(c -> c.putAll(Map.of()))
                .build();
        List<SimpleGrantedAuthority> authorities = java.util.Arrays.stream(roles)
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .toList();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(jwt, null, authorities));
    }

    // --- read ---

    @Test
    void getAllPublished_shouldQueryOnlyActiveStatuses() {
        Event event = Event.builder().id(1L).name("Music Show").status(EventStatus.OPENING).build();
        EventResponse eventResponse = EventResponse.builder().id(1L).name("Music Show").status(EventStatus.OPENING).build();
        PageRequest pageRequest = PageRequest.of(0, 10);

        when(eventRepository.findByStatusIn(anyList(), eq(pageRequest)))
                .thenReturn(new PageImpl<>(List.of(event)));
        when(eventMapper.toEventResponse(event)).thenReturn(eventResponse);

        Page<EventResponse> result = eventService.getAllPublished(pageRequest);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<EventStatus>> statusesCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventRepository).findByStatusIn(statusesCaptor.capture(), eq(pageRequest));
        assertEquals(List.of(EventStatus.UPCOMING, EventStatus.OPENING, EventStatus.CLOSED), statusesCaptor.getValue());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getById_shouldThrowAppExceptionWhenNotFound() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());
        AppException ex = assertThrows(AppException.class, () -> eventService.getById(999L));
        assertEquals(ErrorCode.EVENT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getById_shouldReturnMappedResponse() {
        Event event = Event.builder().id(1L).name("Music Show").build();
        EventResponse eventResponse = EventResponse.builder().id(1L).name("Music Show").build();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventMapper.toEventResponse(event)).thenReturn(eventResponse);

        EventResponse result = eventService.getById(1L);
        assertEquals(1L, result.getId());
        assertEquals("Music Show", result.getName());
    }

    // --- create ---

    @Test
    void create_shouldPersistEventWithPendingStatusWhenNoneProvided() {
        setAuth(42L, "ORGANIZER");
        Category cat = Category.builder().id(1L).name("Music").build();
        EventRequest request = EventRequest.builder()
                .name("New Event")
                .categoryId(1L)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> {
            Event e = inv.getArgument(0);
            e = Event.builder().id(10L).name(e.getName()).status(e.getStatus()).category(e.getCategory()).build();
            return e;
        });
        EventResponse expected = EventResponse.builder().id(10L).name("New Event").status(EventStatus.PENDING).build();
        when(eventMapper.toEventResponse(any())).thenReturn(expected);

        EventResponse result = eventService.create(request);

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());
        assertEquals(EventStatus.PENDING, captor.getValue().getStatus());
        assertEquals(42L, captor.getValue().getOrganizerId());
        assertEquals("New Event", result.getName());
    }

    // --- update ---

    @Test
    void update_shouldThrowForbiddenWhenOrganizerIsNotOwner() {
        setAuth(99L, "ORGANIZER");
        Event event = Event.builder().id(1L).organizerId(42L).build();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        EventRequest request = EventRequest.builder()
                .name("Hacked")
                .categoryId(1L)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .build();

        AppException ex = assertThrows(AppException.class, () -> eventService.update(1L, request));
        assertEquals(ErrorCode.EVENT_FORBIDDEN, ex.getErrorCode());
    }

    @Test
    void update_shouldAllowAdminToUpdateAnyEvent() {
        setAuth(1L, "ADMIN");
        Category cat = Category.builder().id(2L).name("Tech").build();
        Event event = Event.builder().id(5L).organizerId(99L).images(new java.util.ArrayList<>()).build();
        EventRequest request = EventRequest.builder()
                .name("Updated")
                .categoryId(2L)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(2))
                .build();

        when(eventRepository.findById(5L)).thenReturn(Optional.of(event));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(cat));
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toEventResponse(event)).thenReturn(EventResponse.builder().id(5L).build());

        EventResponse result = eventService.update(5L, request);
        assertNotNull(result);
        verify(eventRepository).save(event);
    }

    // --- updateStatus ---

    @Test
    void updateStatus_shouldChangeEventStatus() {
        setAuth(1L, "ADMIN");
        Event event = Event.builder().id(3L).status(EventStatus.PENDING).build();
        when(eventRepository.findById(3L)).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toEventResponse(event)).thenReturn(
                EventResponse.builder().id(3L).status(EventStatus.UPCOMING).build());

        EventResponse result = eventService.updateStatus(3L, EventStatus.UPCOMING);
        assertEquals(EventStatus.UPCOMING, event.getStatus());
        assertEquals(EventStatus.UPCOMING, result.getStatus());
    }

    @Test
    void updateStatus_shouldThrowWhenEventNotFound() {
        setAuth(1L, "ADMIN");
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());
        AppException ex = assertThrows(AppException.class, () -> eventService.updateStatus(999L, EventStatus.UPCOMING));
        assertEquals(ErrorCode.EVENT_NOT_FOUND, ex.getErrorCode());
    }
}
