package com.drag.foreignnationals.etranger.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String street;
    private String city;
    private String zipCode;

    private boolean current;

    @NotNull
    @Column(nullable = false)
    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;

    @NotNull
    @Column(nullable = false)
    @ManyToOne
    @JoinColumn(name = "commune_id")
    private Commune commune;


}