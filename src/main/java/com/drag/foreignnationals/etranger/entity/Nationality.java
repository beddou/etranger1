package com.drag.foreignnationals.etranger.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @OneToOne(mappedBy = "nationality")
    private Person person;
}
