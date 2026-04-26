package com.sa.eventservice.service;

import com.sa.eventservice.dto.response.EventResponse;
import com.sa.eventservice.exception.AppException;
import com.sa.eventservice.exception.ErrorCode;
import com.sa.eventservice.mapper.EventMapper;
import com.sa.eventservice.model.entity.Event;
import com.sa.eventservice.model.enums.EventStatus;
import com.sa.eventservice.repository.EventRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventService {

    EventRepository eventRepository;
    EventMapper eventMapper;

    public Page<EventResponse> getAllPublished(PageRequest pageRequest) {
        List<EventStatus> activeStatuses = List.of(
                EventStatus.UPCOMING,
                EventStatus.OPENING,
                EventStatus.CLOSED
        );
        Page<Event> events = eventRepository.findByStatusIn(activeStatuses, pageRequest);
        return events.map(eventMapper::toEventResponse);
    }

    public EventResponse getById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return eventMapper.toEventResponse(event);
    }
}

