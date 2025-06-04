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

    @ManyToOne
    @JoinColumn(name = "nationality_id")
    private Nationality nationality;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResidencePermit> residencePermits;

    @ManyToOne
    @JoinColumn(name = "situation_id")
    private Situation situation;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses;


}
