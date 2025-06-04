package com.drag.foreignnationals.etranger.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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

    @OneToMany(mappedBy = "commune", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses;
}
