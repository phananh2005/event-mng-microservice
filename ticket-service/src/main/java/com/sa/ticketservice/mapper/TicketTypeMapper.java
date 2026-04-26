package com.sa.ticketservice.mapper;

import com.sa.ticketservice.dto.response.TicketTypeResponse;
import com.sa.ticketservice.model.entity.TicketType;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TicketTypeMapper {
    TicketTypeResponse toResponse(TicketType ticketType);
}
