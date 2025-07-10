package com.drag.foreignnationals.etranger.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;


import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResidencePermit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String permitType;

    @NotNull
    @Column(nullable = false)
    private LocalDate dateOfIssue;

    @NotNull
    @Column(nullable = false)
    private int durationInMonths;

    @NotNull
    @Column(nullable = false)
    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;
}