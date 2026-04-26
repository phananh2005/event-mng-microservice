package com.sa.eventservice.mapper;

import com.sa.eventservice.dto.response.TicketTypeResponse;
import com.sa.eventservice.model.entity.TicketType;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TicketTypeMapper {
    TicketTypeResponse toTicketTypeResponse(TicketType ticketType);
}

