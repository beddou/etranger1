package com.drag.foreignnationals.etranger.entity;

import jakarta.persistence.*;
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

    private LocalDate dateOfIssue;
    private int durationInMonths;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;
}