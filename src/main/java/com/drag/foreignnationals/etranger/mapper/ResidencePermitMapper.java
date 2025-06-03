package com.drag.foreignnationals.etranger.mapper;

import com.drag.foreignnationals.etranger.dto.ResidencePermitDTO;
import com.drag.foreignnationals.etranger.entity.ResidencePermit;
import org.springframework.stereotype.Component;

@Component
public class ResidencePermitMapper {
    public ResidencePermitDTO toDTO(ResidencePermit entity) {
        return ResidencePermitDTO.builder()
                .id(entity.getId())
                .dateOfIssue(entity.getDateOfIssue())
                .durationInMonths(entity.getDurationInMonths())
                .build();
    }

    public ResidencePermit toEntity(ResidencePermitDTO dto) {
        return ResidencePermit.builder()
                .id(dto.getId())
                .dateOfIssue(dto.getDateOfIssue())
                .durationInMonths(dto.getDurationInMonths())
                .build();
    }
}
