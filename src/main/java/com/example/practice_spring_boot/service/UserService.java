package com.example.practice_spring_boot.service;

import com.example.practice_spring_boot.dto.request.UserCreationRequest;
import com.example.practice_spring_boot.dto.request.UserUpdateRequest;
import com.example.practice_spring_boot.dto.response.UserResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface UserService {
    public UserResponse createUser(UserCreationRequest request);

    public UserResponse getMyInfo();

    public UserResponse updateUser(String userId, UserUpdateRequest request);

    public void deleteUser(String userId);

    public List<UserResponse> getUsers();

    public UserResponse getUser(String id);
}
