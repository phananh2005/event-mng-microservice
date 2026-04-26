package com.sa.eventservice.mapper;

import com.sa.eventservice.dto.response.CategoryResponse;
import com.sa.eventservice.model.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {
    CategoryResponse toCategoryResponse(Category category);
}

