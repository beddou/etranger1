package com.drag.foreignnationals.etranger.mapper;

import com.drag.foreignnationals.etranger.dto.NationalityDTO;
import com.drag.foreignnationals.etranger.dto.PersonDTO;
import com.drag.foreignnationals.etranger.entity.Person;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@Mapper(componentModel = "spring", uses = {ResidencePermitMapper.class, AddressMapper.class})
public interface PersonMapper {

    PersonDTO toDTO(Person person);

    @Mapping(target = "residencePermits", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    Person toEntity(PersonDTO dto);
}
