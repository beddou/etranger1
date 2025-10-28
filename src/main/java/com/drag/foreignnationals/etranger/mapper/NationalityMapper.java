package com.drag.foreignnationals.etranger.mapper;

import com.drag.foreignnationals.etranger.dto.NationalityDTO;
import com.drag.foreignnationals.etranger.entity.Nationality;
import org.mapstruct.Mapper;

import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface NationalityMapper {

    NationalityDTO toDTO(Nationality entity) ;

    Nationality toEntity(NationalityDTO dto);
}
