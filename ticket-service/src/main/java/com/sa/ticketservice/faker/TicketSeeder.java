package com.sa.ticketservice.faker;

import com.sa.ticketservice.model.entity.Ticket;
import com.sa.ticketservice.model.entity.TicketType;
import com.sa.ticketservice.model.enums.TicketStatus;
import com.sa.ticketservice.repository.TicketRepository;
import com.sa.ticketservice.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TicketSeeder {

    private final TicketRepository ticketRepository;
    private final TicketTypeRepository ticketTypeRepository;

    public void seed() {
        if (ticketRepository.count() > 0) return;

        List<TicketType> ticketTypes = ticketTypeRepository.findAll();
        if (ticketTypes.isEmpty()) return;

        List<Ticket> tickets = new ArrayList<>();
        long orderId = 1L;
        long customerId = 3L; // customer1
        for (TicketType tt : ticketTypes) {
            for (int i = 0; i < 2; i++) {
                tickets.add(Ticket.builder()
                        .orderId(orderId)
                        .customerId(customerId)
                        .ticketType(tt)
                        .ticketCode("TK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                        .status(TicketStatus.VALID)
                        .build());
            }
            orderId++;
        }
        ticketRepository.saveAll(tickets);
    }
}
