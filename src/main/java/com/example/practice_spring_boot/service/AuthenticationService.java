package com.example.practice_spring_boot.service;

import com.example.practice_spring_boot.dto.request.AuthenticationRequest;
import com.example.practice_spring_boot.dto.request.IntrospectRequest;
import com.example.practice_spring_boot.dto.request.LogoutRequest;
import com.example.practice_spring_boot.dto.request.RefreshRequest;
import com.example.practice_spring_boot.dto.response.AuthenticationResponse;
import com.example.practice_spring_boot.dto.response.IntrospectResponse;
import com.example.practice_spring_boot.model.User;
import com.nimbusds.jose.JOSEException;
import org.springframework.stereotype.Component;

import java.text.ParseException;

@Component
public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request);

    String generateToken(User user);

    void logout(LogoutRequest request) throws ParseException, JOSEException;

    AuthenticationResponse refresh(RefreshRequest request) throws ParseException, JOSEException;

    IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException;
}
