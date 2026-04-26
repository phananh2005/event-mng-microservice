package com.sa.checkinservice.mapper;

import com.sa.checkinservice.dto.response.CheckinLogResponse;
import com.sa.checkinservice.model.entity.CheckinLog;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CheckinLogMapper {
    CheckinLogResponse toResponse(CheckinLog log);
}
