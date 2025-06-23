package com.drag.foreignnationals.etranger.mapper;

import com.drag.foreignnationals.etranger.dto.AddressDTO;
import com.drag.foreignnationals.etranger.entity.Address;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring", uses = {CommuneMapper.class})
public interface AddressMapper {

    AddressDTO toDTO(Address entity);
    Address toEntity(AddressDTO dto);
}
