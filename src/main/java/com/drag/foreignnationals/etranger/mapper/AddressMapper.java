package com.drag.foreignnationals.etranger.mapper;

import com.drag.foreignnationals.etranger.dto.AddressCreateDto;
import com.drag.foreignnationals.etranger.dto.AddressDTO;
import com.drag.foreignnationals.etranger.dto.PersonDTO;
import com.drag.foreignnationals.etranger.entity.Address;
import com.drag.foreignnationals.etranger.entity.Person;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring", uses = {CommuneMapper.class, PersonMapper.class})
public interface AddressMapper {

    AddressDTO toDTO(Address entity);

    @Mapping(target = "person", ignore = true)
    @Mapping(target = "current", ignore = true)
    @Mapping(target = "commune", ignore = true)
    @Mapping(target = "id", ignore = true)
    Address toEntity(AddressCreateDto dto);

    @Mapping(target = "person", ignore = true)
    @Mapping(target = "current", ignore = true)
    @Mapping(target = "commune", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateAddressFromDto(AddressCreateDto dto, @MappingTarget Address entity);



}
