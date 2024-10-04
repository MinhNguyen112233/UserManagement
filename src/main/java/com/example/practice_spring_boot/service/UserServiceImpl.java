package com.example.practice_spring_boot.service;

import com.example.practice_spring_boot.constant.PredefinedRole;
import com.example.practice_spring_boot.dto.request.UserCreationRequest;
import com.example.practice_spring_boot.dto.request.UserUpdateRequest;
import com.example.practice_spring_boot.dto.response.UserResponse;
import com.example.practice_spring_boot.exception.AppException;
import com.example.practice_spring_boot.exception.ErrorCode;
import com.example.practice_spring_boot.mapper.UserMapper;
import com.example.practice_spring_boot.model.Role;
import com.example.practice_spring_boot.model.User;
import com.example.practice_spring_boot.repository.RoleRepository;
import com.example.practice_spring_boot.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;


    @Override
    public UserResponse createUser(UserCreationRequest request) {
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);
        user.setRoles(roles);

       try{
           user = userRepository.save(user);
       }catch(DataIntegrityViolationException e){
            throw new AppException(ErrorCode.USER_EXISTED);
       }

        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponse getMyInfo() {
       var context =  SecurityContextHolder.getContext();
       //tên người đang đăng nhập
       String name = context.getAuthentication().getName();

       User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));


       return userMapper.toUserResponse(user);
    }


    @Override
    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        //User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        var roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    //kiểm tra trước lúc gọi hàm, nếu như là admin thì mới gọi hàm

    @Override
    @PreAuthorize("hasRole('ADMIN')") // 'APPROVE_POST'
    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }


    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(String id) {
        //return userMapper.toUserResponse(
        //                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
        UserResponse response = userMapper.toUserResponse(userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
        log.info("Return username: " + response.getUsername());
        return response;
    }
}
