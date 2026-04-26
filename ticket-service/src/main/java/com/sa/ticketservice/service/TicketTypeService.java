package com.sa.ticketservice.service;

import com.sa.ticketservice.dto.request.ReserveRequest;
import com.sa.ticketservice.dto.response.TicketTypeResponse;
import com.sa.ticketservice.exception.AppException;
import com.sa.ticketservice.exception.ErrorCode;
import com.sa.ticketservice.mapper.TicketTypeMapper;
import com.sa.ticketservice.model.entity.TicketType;
import com.sa.ticketservice.repository.TicketTypeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TicketTypeService {

    TicketTypeRepository ticketTypeRepository;
    TicketTypeMapper ticketTypeMapper;

    public List<TicketTypeResponse> getByEvent(Long eventId) {
        return ticketTypeRepository.findByEventId(eventId).stream()
                .map(ticketTypeMapper::toResponse)
                .toList();
    }

    public TicketTypeResponse getById(Long id) {
        return ticketTypeMapper.toResponse(
                ticketTypeRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.TICKET_TYPE_NOT_FOUND)));
    }

    @Transactional
    public void reserve(ReserveRequest request) {
        TicketType tt = ticketTypeRepository.findById(request.getTicketTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_TYPE_NOT_FOUND));

        if (tt.getRemainingQuantity() < request.getQuantity()) {
            throw new AppException(ErrorCode.TICKET_NOT_ENOUGH);
        }
        tt.setRemainingQuantity(tt.getRemainingQuantity() - request.getQuantity());
        ticketTypeRepository.save(tt);
    }

    @Transactional
    public void release(ReserveRequest request) {
        TicketType tt = ticketTypeRepository.findById(request.getTicketTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.TICKET_TYPE_NOT_FOUND));
        tt.setRemainingQuantity(tt.getRemainingQuantity() + request.getQuantity());
        ticketTypeRepository.save(tt);
    }
}
