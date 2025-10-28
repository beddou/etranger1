package com.drag.foreignnationals.etranger.mapper;


import com.drag.foreignnationals.etranger.dto.CommuneDTO;
import com.drag.foreignnationals.etranger.entity.Commune;
import org.mapstruct.Mapper;

import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface CommuneMapper {

    CommuneDTO toDTO(Commune entity) ;


    Commune toEntity(CommuneDTO dto);

}
