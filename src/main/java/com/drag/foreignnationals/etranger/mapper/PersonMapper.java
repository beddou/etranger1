package com.drag.foreignnationals.etranger.mapper;

import com.drag.foreignnationals.etranger.dto.PersonDTO;
import com.drag.foreignnationals.etranger.entity.Person;
import org.mapstruct.Mapper;

import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring", uses = {ResidencePermitMapper.class, AddressMapper.class,
SituationMapper.class, NationalityMapper.class})
public interface PersonMapper {

    PersonDTO toDTO(Person person);

    Person toEntity(PersonDTO dto);
}
