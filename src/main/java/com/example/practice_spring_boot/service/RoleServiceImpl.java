package com.example.practice_spring_boot.service;

import com.example.practice_spring_boot.dto.request.RoleRequest;
import com.example.practice_spring_boot.dto.response.RoleResponse;
import com.example.practice_spring_boot.mapper.RoleMapper;
import com.example.practice_spring_boot.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleServiceImpl implements RoleService{

    RoleRepository roleRepository;
    //PermissionRepository permissionRepository;
    RoleMapper roleMapper;

    @Override
    public RoleResponse create(RoleRequest request) {
        var role = roleMapper.toRole(request);

//        var permissions = permissionRepository.findAllById(request.getPermissions());
//        role.setPermissions(new HashSet<>(permissions));

        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    @Override
    public List<RoleResponse> getAll() {
        return roleRepository.findAll().stream().map(roleMapper::toRoleResponse).toList();
    }

    @Override
    public void delete(String role) {
        roleRepository.deleteById(role);
    }
}
