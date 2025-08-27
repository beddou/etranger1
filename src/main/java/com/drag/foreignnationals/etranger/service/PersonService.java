package com.drag.foreignnationals.etranger.service;

import com.drag.foreignnationals.etranger.dto.PersonDTO;
import com.drag.foreignnationals.etranger.dto.PersonDetailDTO;

import java.util.List;

public interface PersonService {
    PersonDetailDTO createPerson(PersonDetailDTO dto);
    PersonDetailDTO getPerson(Long id);
    List<PersonDTO> getAllPersons();
    PersonDetailDTO updatePerson(Long id, PersonDetailDTO dto);
    void deletePerson(Long id);
}