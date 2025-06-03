package com.drag.foreignnationals.etranger.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commune {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String nameAr;
    private String code;

    @OneToOne(mappedBy = "commune")
    private Address address;
}
