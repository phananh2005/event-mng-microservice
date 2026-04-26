package com.sa.eventservice.faker;

import com.sa.eventservice.model.entity.Category;
import com.sa.eventservice.model.entity.Event;
import com.sa.eventservice.model.enums.EventStatus;
import com.sa.eventservice.repository.CategoryRepository;
import com.sa.eventservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EventSeeder {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    public void seed() {
        if (eventRepository.count() > 0) return;

        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();

        eventRepository.saveAll(List.of(
                Event.builder()
                        .name("Summer Music Festival")
                        .category(categories.get(0))
                        .organizerId(2L)
                        .location("Hanoi Opera House")
                        .startTime(now.plusDays(10))
                        .endTime(now.plusDays(10).plusHours(5))
                        .saleStartDate(now.minusDays(5))
                        .saleEndDate(now.plusDays(9))
                        .description("A grand outdoor music festival.")
                        .status(EventStatus.UPCOMING)
                        .build(),
                Event.builder()
                        .name("Vietnam Tech Summit 2025")
                        .category(categories.get(1))
                        .organizerId(2L)
                        .location("National Convention Center, Hanoi")
                        .startTime(now.plusDays(20))
                        .endTime(now.plusDays(21))
                        .saleStartDate(now.minusDays(10))
                        .saleEndDate(now.plusDays(19))
                        .description("Annual technology conference for developers and innovators.")
                        .status(EventStatus.UPCOMING)
                        .build(),
                Event.builder()
                        .name("National Football Championship")
                        .category(categories.get(2))
                        .organizerId(2L)
                        .location("My Dinh Stadium, Hanoi")
                        .startTime(now.plusDays(5))
                        .endTime(now.plusDays(5).plusHours(2))
                        .saleStartDate(now.minusDays(3))
                        .saleEndDate(now.plusDays(4))
                        .description("Top football clubs compete for the national title.")
                        .status(EventStatus.OPENING)
                        .build()
        ));
    }
}
