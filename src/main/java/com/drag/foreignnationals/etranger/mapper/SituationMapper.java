package com.drag.foreignnationals.etranger.mapper;

import com.drag.foreignnationals.etranger.dto.SituationDTO;
import com.drag.foreignnationals.etranger.entity.Situation;
import org.springframework.stereotype.Component;

@Component
public class SituationMapper {
    public SituationDTO toDTO(Situation entity) {
        return SituationDTO.builder()
                .id(entity.getId())
                .type(entity.getType())
                .date(entity.getDate())
                .comment(entity.getComment())
                .build();
    }

    public Situation toEntity(SituationDTO dto) {
        return Situation.builder()
                .id(dto.getId())
                .type(dto.getType())
                .date(dto.getDate())
                .comment(dto.getComment())
                .build();
    }
}
