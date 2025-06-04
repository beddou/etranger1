package com.drag.foreignnationals.etranger.dto;

import com.drag.foreignnationals.etranger.entity.Person.Gender;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private NationalityDTO nationality;
    private List<ResidencePermitDTO> residencePermits;
    private SituationDTO situation;
    private List<AddressDTO> addresses;
}