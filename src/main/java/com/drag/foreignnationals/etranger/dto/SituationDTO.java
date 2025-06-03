package com.drag.foreignnationals.etranger.dto;

import com.drag.foreignnationals.etranger.enums.SituationType;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SituationDTO {
    private Long id;
    private SituationType type;
    private LocalDate date;
    private String comment;
}