package com.example.practice_spring_boot.service;

import com.example.practice_spring_boot.dto.request.RoleRequest;
import com.example.practice_spring_boot.dto.response.RoleResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface RoleService {
    public RoleResponse create(RoleRequest request);

    public List<RoleResponse> getAll();

    public void delete(String role);
}
