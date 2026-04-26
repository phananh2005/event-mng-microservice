package com.sa.eventservice.faker;

import com.sa.eventservice.model.entity.Event;
import com.sa.eventservice.model.entity.TicketType;
import com.sa.eventservice.repository.EventRepository;
import com.sa.eventservice.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TicketTypeSeeder {

    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;

    public void seed() {
        if (ticketTypeRepository.count() > 0) return;

        List<Event> events = eventRepository.findAll();
        if (events.isEmpty()) return;

        List<TicketType> ticketTypes = new ArrayList<>();
        for (Event event : events) {
            ticketTypes.add(TicketType.builder()
                    .event(event)
                    .name("General Admission")
                    .price(BigDecimal.valueOf(150000))
                    .totalQuantity(200)
                    .remainingQuantity(200)
                    .description("Standard entry ticket.")
                    .build());
            ticketTypes.add(TicketType.builder()
                    .event(event)
                    .name("VIP")
                    .price(BigDecimal.valueOf(500000))
                    .totalQuantity(50)
                    .remainingQuantity(50)
                    .description("VIP access with exclusive perks.")
                    .build());
        }
        ticketTypeRepository.saveAll(ticketTypes);
    }
}
