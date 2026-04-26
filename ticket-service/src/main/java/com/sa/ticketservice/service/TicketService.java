package com.sa.ticketservice.service;

import com.sa.ticketservice.dto.request.IssueTicketRequest;
import com.sa.ticketservice.dto.response.TicketResponse;
import com.sa.ticketservice.exception.AppException;
import com.sa.ticketservice.exception.ErrorCode;
import com.sa.ticketservice.mapper.TicketMapper;
import com.sa.ticketservice.model.entity.Ticket;
import com.sa.ticketservice.model.entity.TicketType;
import com.sa.ticketservice.model.enums.TicketStatus;
import com.sa.ticketservice.repository.TicketRepository;
import com.sa.ticketservice.repository.TicketTypeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TicketService {

    TicketRepository ticketRepository;
    TicketTypeRepository ticketTypeRepository;
    TicketMapper ticketMapper;

    public List<TicketResponse> getMyTickets() {
        Long customerId = extractUserId();
        return ticketRepository.findByCustomerId(customerId).stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Transactional
    public List<TicketResponse> issue(IssueTicketRequest request) {
        TicketType tt = ticketTypeRepository.findById(request.getTicketTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_TYPE_NOT_FOUND));

        List<Ticket> issued = new ArrayList<>();
        for (int i = 0; i < request.getQuantity(); i++) {
            String code = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            issued.add(ticketRepository.save(Ticket.builder()
                    .orderId(request.getOrderId())
                    .customerId(request.getCustomerId())
                    .ticketType(tt)
                    .ticketCode(code)
                    .qrCode("https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=" + code)
                    .status(TicketStatus.VALID)
                    .build()));
        }
        return issued.stream().map(ticketMapper::toResponse).toList();
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public TicketResponse checkIn(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_NOT_FOUND));

        if (ticket.getStatus() == TicketStatus.USED) {
            throw new AppException(ErrorCode.TICKET_USED);
        }
        if (ticket.getStatus() != TicketStatus.VALID) {
            throw new AppException(ErrorCode.TICKET_INVALID);
        }

        ticket.setStatus(TicketStatus.USED);
        ticket.setUsedAt(LocalDateTime.now());
        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    private Long extractUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.parseLong(jwt.getSubject());
    }
}
