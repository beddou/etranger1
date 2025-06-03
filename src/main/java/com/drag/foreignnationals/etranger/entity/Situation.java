package com.drag.foreignnationals.etranger.entity;


import com.drag.foreignnationals.etranger.enums.SituationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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

    @OneToOne(mappedBy = "situation")
    private Person person;
}
