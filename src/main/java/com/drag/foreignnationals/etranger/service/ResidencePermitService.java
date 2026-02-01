package com.drag.foreignnationals.etranger.service;

import com.drag.foreignnationals.etranger.dto.ResidencePermitDTO;

import java.util.List;


public interface ResidencePermitService {
    ResidencePermitDTO create(Long personId, ResidencePermitDTO dto);
    ResidencePermitDTO update(Long personId, Long permitId, ResidencePermitDTO dto);
    void delete(Long personId, Long permitId);
    List<ResidencePermitDTO> getByPersonId(Long personId);
    ResidencePermitDTO getById(Long personId, Long permitId);
}
