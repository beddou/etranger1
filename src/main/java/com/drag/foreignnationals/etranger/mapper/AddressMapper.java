package com.drag.foreignnationals.etranger.mapper;

import com.drag.foreignnationals.etranger.dto.AddressDTO;
import com.drag.foreignnationals.etranger.dto.PersonDTO;
import com.drag.foreignnationals.etranger.entity.Address;
import com.drag.foreignnationals.etranger.entity.Person;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring", uses = {CommuneMapper.class, PersonMapper.class})
public interface AddressMapper {
    @Mapping(target = "person", ignore = true)
    AddressDTO toDTO(Address entity);
    Address toEntity(AddressDTO dto);

    // Updates existing entity instead of creating a new one
    //@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAddressFromDto(AddressDTO dto, @MappingTarget Address entity);



}
