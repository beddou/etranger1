package com.drag.foreignnationals.etranger.dto;

import com.drag.foreignnationals.etranger.enums.ResidenceType;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResidencePermitDTO {
    private Long id;
    private ResidenceType type;
    private LocalDate dateOfIssue;
    private int durationInMonths;
    private PersonDTO person;
}
