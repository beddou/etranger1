package com.drag.foreignnationals.etranger.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String firstName;

    @NotNull
    @Column(nullable = false)
    private String lastName;

    @NotNull
    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;


    public enum Gender {
        MALE, FEMALE
    }

    @NotNull
    @Column(nullable = false)
    @ManyToOne
    @JoinColumn(name = "nationality_id")
    private Nationality nationality;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResidencePermit> residencePermits;

    @OneToOne(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)

    private Situation situation;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses;

    public Address getCurrentAddress() {
        return addresses.stream()
                .filter(Address::isCurrent)              // assuming you have a boolean flag
                .findFirst() //
                .orElse(null);
    }

    public ResidencePermit getLastResidencePermit() {
        return residencePermits.stream()
                .max(Comparator.comparing(ResidencePermit::getDateOfIssue)) // or expiryDate
                .orElse(null);
    }


}
