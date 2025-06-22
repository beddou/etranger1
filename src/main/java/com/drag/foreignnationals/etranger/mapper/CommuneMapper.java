package com.drag.foreignnationals.etranger.mapper;


import com.drag.foreignnationals.etranger.dto.CommuneDTO;
import com.drag.foreignnationals.etranger.entity.Commune;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface CommuneMapper {

    @Mapping(target = "addresses", ignore = true)
    CommuneDTO toDTO(Commune entity) ;

    @Mapping(target = "addresses", ignore = true)
    Commune toEntity(CommuneDTO dto);

}
