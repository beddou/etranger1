package com.drag.foreignnationals.etranger.service;

import com.drag.foreignnationals.etranger.dto.ResidencePermitDTO;

import java.util.List;

public interface ResidencePermitService {
    ResidencePermitDTO create(ResidencePermitDTO dto);
    ResidencePermitDTO get(Long id);
    List<ResidencePermitDTO> getAll();
    ResidencePermitDTO update(Long id, ResidencePermitDTO dto);
    void delete(Long id);
}
