package com.example.practice_spring_boot.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
//lớp này để verify jwt
public class IntrospectRequest {
    String token;
}
