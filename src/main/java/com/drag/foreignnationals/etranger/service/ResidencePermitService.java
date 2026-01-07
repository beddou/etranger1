package com.drag.foreignnationals.etranger.service;

import com.drag.foreignnationals.etranger.dto.ResidencePermitDTO;
import com.drag.foreignnationals.etranger.entity.Person;
import org.springframework.stereotype.Service;

import java.util.List;


public interface ResidencePermitService {
    ResidencePermitDTO create( ResidencePermitDTO dto);
    ResidencePermitDTO update(Long id, ResidencePermitDTO dto);
    void delete(Long id);
    List<ResidencePermitDTO> getByPersonId(Long personId);
    ResidencePermitDTO getById(Long id);
}
