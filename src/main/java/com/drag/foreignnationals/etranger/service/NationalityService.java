package com.drag.foreignnationals.etranger.service;

import com.drag.foreignnationals.etranger.dto.NationalityDTO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface NationalityService {

    List<NationalityDTO> getAll();

}

