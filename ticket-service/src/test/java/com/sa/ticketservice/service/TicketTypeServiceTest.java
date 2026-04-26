package com.sa.ticketservice.service;

import com.sa.ticketservice.dto.request.ReserveRequest;
import com.sa.ticketservice.exception.AppException;
import com.sa.ticketservice.exception.ErrorCode;
import com.sa.ticketservice.mapper.TicketTypeMapper;
import com.sa.ticketservice.model.entity.TicketType;
import com.sa.ticketservice.repository.TicketTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketTypeServiceTest {

    @Mock TicketTypeRepository ticketTypeRepository;
    @Mock TicketTypeMapper ticketTypeMapper;
    @InjectMocks TicketTypeService ticketTypeService;

    private TicketType ticketType(int remaining) {
        return TicketType.builder()
                .id(1L).eventId(10L).name("VIP")
                .price(BigDecimal.valueOf(100))
                .totalQuantity(50).remainingQuantity(remaining)
                .build();
    }

    @Test
    void reserve_shouldDeductRemainingQuantity() {
        TicketType tt = ticketType(10);
        when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(tt));

        ticketTypeService.reserve(ReserveRequest.builder().ticketTypeId(1L).quantity(3).build());

        ArgumentCaptor<TicketType> captor = ArgumentCaptor.forClass(TicketType.class);
        verify(ticketTypeRepository).save(captor.capture());
        assertEquals(7, captor.getValue().getRemainingQuantity());
    }

    @Test
    void reserve_shouldThrowWhenNotEnoughStock() {
        TicketType tt = ticketType(2);
        when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(tt));

        AppException ex = assertThrows(AppException.class,
                () -> ticketTypeService.reserve(ReserveRequest.builder().ticketTypeId(1L).quantity(5).build()));
        assertEquals(ErrorCode.TICKET_NOT_ENOUGH, ex.getErrorCode());
        verify(ticketTypeRepository, never()).save(any());
    }

    @Test
    void release_shouldRestoreRemainingQuantity() {
        TicketType tt = ticketType(7);
        when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(tt));

        ticketTypeService.release(ReserveRequest.builder().ticketTypeId(1L).quantity(3).build());

        ArgumentCaptor<TicketType> captor = ArgumentCaptor.forClass(TicketType.class);
        verify(ticketTypeRepository).save(captor.capture());
        assertEquals(10, captor.getValue().getRemainingQuantity());
    }

    @Test
    void reserve_shouldThrowWhenTicketTypeNotFound() {
        when(ticketTypeRepository.findById(99L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> ticketTypeService.reserve(ReserveRequest.builder().ticketTypeId(99L).quantity(1).build()));
        assertEquals(ErrorCode.TICKET_TYPE_NOT_FOUND, ex.getErrorCode());
    }
}
