package com.example.practice_spring_boot.service;

import com.example.practice_spring_boot.dto.request.PermissionRequest;
import com.example.practice_spring_boot.dto.response.PermissionResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface PermissionService {
    public PermissionResponse create(PermissionRequest request);

    public List<PermissionResponse> getAll();

    public void delete(String permission);
}
