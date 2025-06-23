package com.drag.foreignnationals.etranger.mapper;

import com.drag.foreignnationals.etranger.dto.ResidencePermitDTO;
import com.drag.foreignnationals.etranger.entity.ResidencePermit;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring", uses = {PersonMapper.class})
public interface ResidencePermitMapper {
    ResidencePermitDTO toDTO(ResidencePermit entity);
    ResidencePermit toEntity(ResidencePermitDTO dto);
}
