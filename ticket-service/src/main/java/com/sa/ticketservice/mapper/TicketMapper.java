package com.sa.ticketservice.mapper;

import com.sa.ticketservice.dto.response.TicketResponse;
import com.sa.ticketservice.model.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TicketMapper {

    @Mapping(target = "ticketTypeName", source = "ticketType.name")
    TicketResponse toResponse(Ticket ticket);
}
