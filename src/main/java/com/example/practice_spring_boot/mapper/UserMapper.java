package com.example.practice_spring_boot.mapper;

import com.example.practice_spring_boot.dto.request.UserCreationRequest;
import com.example.practice_spring_boot.dto.request.UserUpdateRequest;
import com.example.practice_spring_boot.dto.response.UserResponse;
import com.example.practice_spring_boot.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest userCreationRequest);

    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest userUpdateRequest);
}
