package com.drag.foreignnationals.etranger.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    public enum Gender {
        MALE, FEMALE
    }

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "nationality_id", referencedColumnName = "id")
    private Nationality nationality;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "residence_permit_id", referencedColumnName = "id")
    private ResidencePermit residencePermit;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL)
    private List<ResidencePermit> residencePermits;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "situation_id", referencedColumnName = "id")
    private Situation situation;


}
