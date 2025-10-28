package com.drag.foreignnationals.etranger.service;

import com.drag.foreignnationals.etranger.dto.PersonCreateDTO;
import com.drag.foreignnationals.etranger.dto.PersonDTO;
import com.drag.foreignnationals.etranger.dto.PersonDetailDTO;

import java.util.List;

public interface PersonService {
    PersonDetailDTO create(PersonCreateDTO dto);
    PersonDetailDTO get(Long id);
    List<PersonDTO> search(String name);
    PersonDetailDTO update(Long id, PersonCreateDTO dto);
    void delete(Long id);
}