package com.sa.event_mng.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.sa.event_mng.dto.request.UserCreateRequest;
import com.sa.event_mng.dto.request.UserUpdateRequest;
import com.sa.event_mng.dto.response.UserResponse;
import com.sa.event_mng.model.entity.Role;
import com.sa.event_mng.model.entity.User;

import org.mapstruct.ReportingPolicy;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "verificationToken", ignore = true)
    User toUser(UserCreateRequest request);

    @Mapping(target = "role", source = "roles")
    @Mapping(target = "roles", source = "roles")
    UserResponse toUserResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "verificationToken", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);

    default com.sa.event_mng.model.enums.Role mapPrimaryRole(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return null;
        }

        return roles.stream()
                .map(Role::getName)
                .sorted(Comparator.comparing(Enum::name))
                .findFirst()
                .orElse(null);
    }

    default List<String> mapRoleNames(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        return roles.stream()
                .map(role -> role.getName().name())
                .sorted()
                .toList();
    }
}
