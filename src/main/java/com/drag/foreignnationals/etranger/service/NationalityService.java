package com.drag.foreignnationals.etranger.service;

import com.drag.foreignnationals.etranger.dto.NationalityDTO;

import java.util.List;

public interface NationalityService {
    NationalityDTO create(NationalityDTO dto);
    NationalityDTO get(Long id);
    List<NationalityDTO> getAll();
    NationalityDTO update(Long id, NationalityDTO dto);
    void delete(Long id);
}
