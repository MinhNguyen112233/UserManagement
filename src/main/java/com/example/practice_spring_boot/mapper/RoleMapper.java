package com.example.practice_spring_boot.mapper;

import com.example.practice_spring_boot.dto.request.RoleRequest;
import com.example.practice_spring_boot.dto.response.RoleResponse;
import com.example.practice_spring_boot.model.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest roleRequest);

    RoleResponse toRoleResponse(Role role);
}
