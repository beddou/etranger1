package com.drag.foreignnationals.etranger.dto;

import com.drag.foreignnationals.etranger.entity.Person;
import lombok.Data;
import java.time.LocalDate;
import java.util.Optional;

@Data
public class PersonPatchDTO {

    private Optional<String> firstName = Optional.empty();
    private Optional<String> lastName = Optional.empty();
    private Optional<LocalDate> dateOfBirth = Optional.empty();
    private Optional<Person.Gender> gender = Optional.empty();

    private Optional<Long> nationalityId = Optional.empty();
    private Optional<Long> situationId = Optional.empty();

    private Optional<AddressCreateDto> currentAddress = Optional.empty();


}