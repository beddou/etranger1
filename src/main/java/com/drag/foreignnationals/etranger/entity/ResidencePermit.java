package com.drag.foreignnationals.etranger.entity;

import com.drag.foreignnationals.etranger.enums.ResidenceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;


import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResidencePermit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ResidenceType type;

    @NotNull
    @Column(nullable = false)
    private LocalDate dateOfIssue;

    @NotNull
    @Column(nullable = false)
    private int durationInMonths;



    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;
}