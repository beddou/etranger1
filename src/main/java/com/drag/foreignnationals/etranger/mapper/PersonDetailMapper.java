package com.drag.foreignnationals.etranger.mapper;

import com.drag.foreignnationals.etranger.dto.AddressDTO;
import com.drag.foreignnationals.etranger.dto.PersonDTO;
import com.drag.foreignnationals.etranger.dto.PersonDetailDTO;
import com.drag.foreignnationals.etranger.dto.ResidencePermitDTO;
import com.drag.foreignnationals.etranger.entity.Address;
import com.drag.foreignnationals.etranger.entity.Person;
import com.drag.foreignnationals.etranger.entity.ResidencePermit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring",
        uses = {
                ResidencePermitMapper.class,
                AddressMapper.class,
                SituationMapper.class,
                NationalityMapper.class
                }
      )
public interface PersonDetailMapper {

    // --- Entity → DTO ---
    @Mapping(target = "currentAddress", source = "address")
    @Mapping(target = "lastResidencePermit", source = "permit")
    PersonDetailDTO toPersonDetailDto(Person person, Address address, ResidencePermit permit);

    // --- DTO → Entity ---
    @Mapping(target = "addresses", ignore = true) // we only map one address, not the list
    @Mapping(target = "residencePermits", ignore = true) // same here
    Person toPerson(PersonDetailDTO dto);

    Address toAddress(PersonDetailDTO dto);
    ResidencePermit toResidencePermit(PersonDetailDTO dto);



}
