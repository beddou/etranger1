package com.drag.foreignnationals.etranger.service;

import com.drag.foreignnationals.etranger.dto.SituationDTO;

import java.util.List;

public interface SituationService {
    SituationDTO create(SituationDTO dto);
    SituationDTO get(Long id);
    List<SituationDTO> getAll();
    SituationDTO update(Long id, SituationDTO dto);
    void delete(Long id);
}
