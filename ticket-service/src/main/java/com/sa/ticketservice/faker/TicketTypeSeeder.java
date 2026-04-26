package com.sa.ticketservice.faker;

import com.sa.ticketservice.model.entity.TicketType;
import com.sa.ticketservice.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TicketTypeSeeder {

    private final TicketTypeRepository ticketTypeRepository;

    public void seed() {
        if (ticketTypeRepository.count() > 0) return;
        ticketTypeRepository.saveAll(List.of(
                TicketType.builder().eventId(1L).name("General Admission").price(BigDecimal.valueOf(150000)).totalQuantity(200).remainingQuantity(200).description("Standard entry ticket.").build(),
                TicketType.builder().eventId(1L).name("VIP").price(BigDecimal.valueOf(500000)).totalQuantity(50).remainingQuantity(50).description("VIP access with exclusive perks.").build(),
                TicketType.builder().eventId(2L).name("General Admission").price(BigDecimal.valueOf(200000)).totalQuantity(300).remainingQuantity(300).description("Standard entry ticket.").build(),
                TicketType.builder().eventId(2L).name("VIP").price(BigDecimal.valueOf(700000)).totalQuantity(30).remainingQuantity(30).description("VIP access with exclusive perks.").build(),
                TicketType.builder().eventId(3L).name("General Admission").price(BigDecimal.valueOf(100000)).totalQuantity(500).remainingQuantity(500).description("Standard entry ticket.").build()
        ));
    }
}
