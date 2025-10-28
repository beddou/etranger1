package com.drag.foreignnationals.etranger.dto;

import com.drag.foreignnationals.etranger.entity.Person;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonCreateDTO {

    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private Person.Gender gender;
    private Long nationalityId;

    private Long situationId;
    private AddressCreateDto currentAddress;
}
