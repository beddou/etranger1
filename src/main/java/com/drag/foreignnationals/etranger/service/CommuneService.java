package com.drag.foreignnationals.etranger.service;

import com.drag.foreignnationals.etranger.dto.CommuneDTO;
import org.springframework.stereotype.Service;


import java.util.List;


public interface CommuneService {
    List<CommuneDTO> getAll();
}
