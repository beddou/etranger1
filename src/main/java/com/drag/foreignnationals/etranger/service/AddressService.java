package com.drag.foreignnationals.etranger.service;

import com.drag.foreignnationals.etranger.dto.AddressCreateDto;
import com.drag.foreignnationals.etranger.dto.AddressDTO;
import com.drag.foreignnationals.etranger.dto.PersonCreateDTO;
import com.drag.foreignnationals.etranger.dto.PersonDetailDTO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface AddressService {
    AddressDTO add(Long idPerson, AddressCreateDto dto);
    List<AddressDTO> getAllByPerson(Long idPerson);
}
