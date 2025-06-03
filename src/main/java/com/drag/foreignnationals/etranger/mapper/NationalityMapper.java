package com.drag.foreignnationals.etranger.mapper;

import com.drag.foreignnationals.etranger.dto.NationalityDTO;
import com.drag.foreignnationals.etranger.entity.Nationality;
import org.springframework.stereotype.Component;

@Component
public class NationalityMapper {
    public NationalityDTO toDTO(Nationality entity) {
        return NationalityDTO.builder()
                .id(entity.getId())
                .country(entity.getCountry())
                .countryAr(entity.getCountryAr())
                .build();
    }

    public Nationality toEntity(NationalityDTO dto) {
        return Nationality.builder()
                .id(dto.getId())
                .country(dto.getCountry())
                .countryAr(dto.getCountryAr())
                .build();
    }
}
