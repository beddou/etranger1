package com.drag.foreignnationals.etranger.entity;

import jakarta.persistence.*;
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

    private String country;
    private String countryAr;

    @OneToMany(mappedBy = "nationality", orphanRemoval = true)
    private List<Person> persons;
}
