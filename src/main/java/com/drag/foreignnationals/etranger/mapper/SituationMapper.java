package com.drag.foreignnationals.etranger.mapper;

import com.drag.foreignnationals.etranger.dto.SituationDTO;
import com.drag.foreignnationals.etranger.entity.Situation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface SituationMapper {

    SituationDTO toDTO(Situation entity);

    @Mapping(target = "person", ignore = true)
    Situation toEntity(SituationDTO dto);
}
