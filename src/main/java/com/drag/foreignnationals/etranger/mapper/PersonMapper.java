package com.drag.foreignnationals.etranger.mapper;

import com.drag.foreignnationals.etranger.dto.NationalityDTO;
import com.drag.foreignnationals.etranger.dto.PersonDTO;
import com.drag.foreignnationals.etranger.entity.Person;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PersonMapper {
    public PersonDTO toDTO(Person person) {

        ResidencePermitMapper residencePermitMapper = new ResidencePermitMapper();

        return PersonDTO.builder()
                .id(person.getId())
                .firstName(person.getFirstName())
                .lastName(person.getLastName())
                .dateOfBirth(person.getDateOfBirth())
                .gender(person.getGender())
                .residencePermits(
                        person.getResidencePermits().stream()
                                .map(residencePermitMapper::toDTO)
                                .collect(Collectors.toList())
                )
                .addresses(
                        person.getAddresses().stream()
                                .map(addressMapper::toDTO)
                                .collect(Collectors.toList())
                )
                .build();
    }

    public Person toEntity(PersonDTO dto) {
        return Person.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .build();
    }
}
