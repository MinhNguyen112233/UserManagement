package com.example.practice_spring_boot.mapper;

import com.example.practice_spring_boot.dto.request.PermissionRequest;
import com.example.practice_spring_boot.dto.response.PermissionResponse;
import com.example.practice_spring_boot.model.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
