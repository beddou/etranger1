package com.drag.foreignnationals.etranger.service;

import com.drag.foreignnationals.etranger.dto.PersonCreateDTO;
import com.drag.foreignnationals.etranger.dto.PersonDTO;
import com.drag.foreignnationals.etranger.dto.PersonDetailDTO;
import com.drag.foreignnationals.etranger.dto.PersonPatchDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PersonService {
    Page<PersonDTO> search(String keyword, int page, int size, String sortBy, String direction);
    PersonDetailDTO create(PersonCreateDTO dto);
    PersonDetailDTO get(Long id);
    PersonDetailDTO update(Long id, PersonCreateDTO dto);
    PersonDetailDTO patch(Long id, PersonPatchDTO dto);
    void delete(Long id);
}