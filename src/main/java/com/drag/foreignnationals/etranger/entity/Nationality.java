package com.drag.foreignnationals.etranger.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Nationality {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String country;
    @NotNull
    @Column(nullable = false)
    private String countryAr;

    @OneToMany(mappedBy = "nationality", orphanRemoval = true)
    private List<Person> persons;
}
