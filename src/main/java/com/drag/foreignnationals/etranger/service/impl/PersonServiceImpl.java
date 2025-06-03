package com.drag.foreignnationals.etranger.service.impl;

import com.drag.foreignnationals.etranger.dto.PersonDTO;
import com.drag.foreignnationals.etranger.entity.Person;
import com.drag.foreignnationals.etranger.exception.ResourceNotFoundException;
import com.drag.foreignnationals.etranger.mapper.PersonMapper;
import com.drag.foreignnationals.etranger.repository.PersonRepository;
import com.drag.foreignnationals.etranger.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    @Override
    public PersonDTO createPerson(PersonDTO dto) {
        return personMapper.toDTO(personRepository.save(personMapper.toEntity(dto)));
    }

    @Override
    public PersonDTO getPerson(Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with ID " + id));
        return personMapper.toDTO(person);
    }

    @Override
    public List<PersonDTO> getAllPersons() {
        return personRepository.findAll()
                .stream()
                .map(personMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PersonDTO updatePerson(Long id, PersonDTO dto) {
        Person existing = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with ID " + id));
        existing.setFirstName(dto.getFirstName());
        existing.setLastName(dto.getLastName());
        existing.setDateOfBirth(dto.getDateOfBirth());
        existing.setGender(dto.getGender());
        return personMapper.toDTO(personRepository.save(existing));
    }

    @Override
    public void deletePerson(Long id) {
        if (!personRepository.existsById(id)) {
            throw new ResourceNotFoundException("Person not found with ID " + id);
        }
        personRepository.deleteById(id);
    }
}