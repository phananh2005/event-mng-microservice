package com.sa.ticketservice.repository;

import com.sa.ticketservice.model.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCustomerId(Long customerId);
    Optional<Ticket> findByTicketCode(String ticketCode);
}
