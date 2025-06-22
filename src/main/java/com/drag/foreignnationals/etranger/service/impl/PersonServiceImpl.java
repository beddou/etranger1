package com.drag.foreignnationals.etranger.service.impl;

import com.drag.foreignnationals.etranger.dto.PersonDTO;
import com.drag.foreignnationals.etranger.dto.ResidencePermitDTO;
import com.drag.foreignnationals.etranger.entity.Address;
import com.drag.foreignnationals.etranger.entity.Person;
import com.drag.foreignnationals.etranger.entity.ResidencePermit;
import com.drag.foreignnationals.etranger.exception.ResourceNotFoundException;
import com.drag.foreignnationals.etranger.mapper.AddressMapper;
import com.drag.foreignnationals.etranger.mapper.PersonMapper;
import com.drag.foreignnationals.etranger.mapper.ResidencePermitMapper;
import com.drag.foreignnationals.etranger.repository.AddressRepository;
import com.drag.foreignnationals.etranger.repository.PersonRepository;
import com.drag.foreignnationals.etranger.repository.ResidencePermitRepository;
import com.drag.foreignnationals.etranger.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    @Autowired
    PersonRepository personRepository;
    @Autowired
    PersonMapper personMapper;
    @Autowired
    ResidencePermitRepository residencePermitRepository;
    @Autowired
    AddressMapper addressMapper;
    @Autowired
    AddressRepository addressRepository;
    @Autowired
    ResidencePermitMapper residencePermitMapper;

    @Override
    public PersonDTO createPerson(PersonDTO dto) {
        Person person = personMapper.toEntity(dto);
        person = personRepository.save(person);

        if (dto.getResidencePermits() != null) {
            List<ResidencePermit> permits = new ArrayList<>();
            for (ResidencePermitDTO residencePermitDTO : dto.getResidencePermits()) {
                ResidencePermit p = residencePermitMapper.toEntity(residencePermitDTO);
                p.setPerson(person);
                permits.add(p);
            }
            residencePermitRepository.saveAll(permits);
            person.setResidencePermits(permits);
        }

        if (dto.getAddresses() != null) {
            Person finalPerson = person;
            List<Address> addresses = dto.getAddresses().stream()
                    .map(addressMapper::toEntity)
                    .peek(a -> a.setPerson(finalPerson))
                    .collect(Collectors.toList());
            addressRepository.saveAll(addresses);
            person.setAddresses(addresses);
        }

        return personMapper.toDTO(personRepository.save(person));
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