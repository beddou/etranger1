package com.drag.foreignnationals.etranger.entity;


import com.drag.foreignnationals.etranger.enums.SituationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Situation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private SituationType type;

    private LocalDate date;

    private String comment;

    @OneToMany(mappedBy = "situation")
    private List<Person> persons;
}
