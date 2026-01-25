package com.drag.foreignnationals.etranger.service;

import com.drag.foreignnationals.etranger.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;



public interface PersonService {
    Page<PersonDTO> search(String keyword, int page, int size, String sortBy, String direction);
    PersonDetailDTO create(PersonCreateDTO dto);
    PersonDetailDTO getById(Long id);
    PersonDetailDTO update(Long id, PersonUpdateDTO dto);
    PersonDetailDTO patch(Long id, PersonPatchDTO dto);
    void delete(Long id);
}