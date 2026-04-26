package com.sa.bookingservice.mapper;

import com.sa.bookingservice.dto.response.OrderResponse;
import com.sa.bookingservice.model.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {
    OrderResponse toOrderResponse(Order order);
}
