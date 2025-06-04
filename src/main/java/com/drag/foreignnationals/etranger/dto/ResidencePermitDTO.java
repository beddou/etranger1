package com.drag.foreignnationals.etranger.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResidencePermitDTO {
    private Long id;
    private LocalDate dateOfIssue;
    private int durationInMonths;
    private PersonDTO person;
}
