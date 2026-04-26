package com.sa.eventservice.service;

import com.sa.eventservice.dto.response.EventResponse;
import com.sa.eventservice.exception.AppException;
import com.sa.eventservice.exception.ErrorCode;
import com.sa.eventservice.mapper.EventMapper;
import com.sa.eventservice.model.entity.Event;
import com.sa.eventservice.model.enums.EventStatus;
import com.sa.eventservice.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventService eventService;

    @Test
    void getAllPublished_shouldQueryOnlyActiveStatuses() {
        Event event = Event.builder().id(1L).name("Music Show").status(EventStatus.OPENING).build();
        EventResponse eventResponse = EventResponse.builder().id(1L).name("Music Show").status(EventStatus.OPENING).build();
        PageRequest pageRequest = PageRequest.of(0, 10);

        when(eventRepository.findByStatusIn(org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.eq(pageRequest)))
                .thenReturn(new PageImpl<>(List.of(event)));
        when(eventMapper.toEventResponse(event)).thenReturn(eventResponse);

        Page<EventResponse> result = eventService.getAllPublished(pageRequest);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<EventStatus>> statusesCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventRepository).findByStatusIn(statusesCaptor.capture(), org.mockito.ArgumentMatchers.eq(pageRequest));
        assertEquals(List.of(EventStatus.UPCOMING, EventStatus.OPENING, EventStatus.CLOSED), statusesCaptor.getValue());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getById_shouldThrowAppExceptionWhenNotFound() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> eventService.getById(999L));

        assertEquals(ErrorCode.EVENT_NOT_FOUND, exception.getErrorCode());
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
}

