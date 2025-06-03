package com.drag.foreignnationals.etranger.service;

import com.drag.foreignnationals.etranger.dto.PersonDTO;

import java.util.List;

public interface PersonService {
    PersonDTO createPerson(PersonDTO dto);
    PersonDTO getPerson(Long id);
    List<PersonDTO> getAllPersons();
    PersonDTO updatePerson(Long id, PersonDTO dto);
    void deletePerson(Long id);
}