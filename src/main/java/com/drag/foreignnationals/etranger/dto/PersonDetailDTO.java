package com.drag.foreignnationals.etranger.dto;

import com.drag.foreignnationals.etranger.entity.Person;
import com.drag.foreignnationals.etranger.enums.ResidenceType;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Entity;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PersonDetailDTO {

    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private Person.Gender gender;
    private NationalityDTO nationality;
    private SituationDTO situation;

    private AddressDTO currentAddress;
    private ResidencePermitDTO lastResidencePermit;

}
