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

    @Mapping(target = "personId", ignore = true)
    ResidencePermitDTO toDTO(ResidencePermit entity);

    @Mapping(target = "person", ignore = true)
    ResidencePermit toEntity(ResidencePermitDTO dto);

    @Mapping(target = "person", ignore = true)
    void updateResidencePermitFromDto(ResidencePermitDTO dto, @MappingTarget ResidencePermit entity);

}
