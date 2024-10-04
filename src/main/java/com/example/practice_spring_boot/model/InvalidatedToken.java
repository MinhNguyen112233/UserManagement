package com.example.practice_spring_boot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
//lớp chứa các token đã bị logout
public class InvalidatedToken {
    @Id
    String id;

    Date expiryTime;
}
