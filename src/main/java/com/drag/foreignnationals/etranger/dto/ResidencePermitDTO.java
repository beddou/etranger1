package com.drag.foreignnationals.etranger.dto;

import com.drag.foreignnationals.etranger.enums.ResidenceType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResidencePermitDTO {
    private Long id;

    @NotNull(message = "Residence permit type is required")
    private ResidenceType type;

    @NotNull(message = "Date of issue is required")
    private LocalDate dateOfIssue;

    @NotNull(message = "Duration in months is required")
    @Min(value = 1, message = "Duration must be at least 1 month")
    private Integer durationInMonths;
    private boolean active;
    private LocalDate expirationDate;
    private Long personId;

}
