package com.drag.foreignnationals.etranger.security.entity;

import com.drag.foreignnationals.etranger.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted = true, deleted_at = NOW(), active = false WHERE id = ?")
@SQLRestriction("deleted = false")

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    @PrePersist
    @PreUpdate
    public void normalize() {
        if (username != null) {
            username = username.strip().toLowerCase(Locale.ROOT);
        }
    }

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role; // USER or ADMIN

    @NotBlank
    @Column(nullable = false)
    private String firstName;

    @NotBlank
    @Column(nullable = false)
    private String lastName;

    @NotNull
    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Builder.Default
    private boolean active = true;   // true = account enabled

    @Builder.Default
    private boolean locked = false;  // true = account locked

    @Builder.Default
    private boolean deleted = false;

    private LocalDateTime deletedAt;

    // Getters & setters
}
