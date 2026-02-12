package com.drag.foreignnationals.etranger.security.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String role;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private boolean active;
    private boolean locked;
}
