package com.example.practice_spring_boot.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Size;

//import com.devteria.identityservice.validator.DobConstraint;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @Size(min = 4, message = "USERNAME_INVALID") // exception
    String username;

    @Size(min = 6, message = "INVALID_PASSWORD") // exception
    String password;

    String firstName;
    String lastName;

    //@DobConstraint(min = 10, message = "INVALID_DOB")
    LocalDate dob;
}
