package com.drag.foreignnationals.etranger.mapper;

import com.drag.foreignnationals.etranger.dto.PersonDTO;
import com.drag.foreignnationals.etranger.dto.ResidencePermitDTO;
import com.drag.foreignnationals.etranger.entity.Person;
import com.drag.foreignnationals.etranger.entity.ResidencePermit;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring", uses = {PersonMapper.class})
public interface ResidencePermitMapper {
    ResidencePermitDTO toDTO(ResidencePermit entity);


    ResidencePermit toEntity(ResidencePermitDTO dto);

    // Updates existing entity instead of creating a new one
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateResidencePermitFromDto(ResidencePermitDTO dto, @MappingTarget ResidencePermit entity);

}
