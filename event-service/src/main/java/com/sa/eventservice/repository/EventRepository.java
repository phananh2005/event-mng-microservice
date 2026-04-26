package com.sa.eventservice.repository;

import com.sa.eventservice.model.entity.Event;
import com.sa.eventservice.model.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByStatusIn(Collection<EventStatus> statuses, Pageable pageable);
}

