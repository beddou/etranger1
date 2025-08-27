package com.drag.foreignnationals.etranger.service.impl;

import com.drag.foreignnationals.etranger.dto.PersonDTO;
import com.drag.foreignnationals.etranger.dto.PersonDetailDTO;
import com.drag.foreignnationals.etranger.dto.ResidencePermitDTO;
import com.drag.foreignnationals.etranger.entity.Address;
import com.drag.foreignnationals.etranger.entity.Person;
import com.drag.foreignnationals.etranger.entity.ResidencePermit;
import com.drag.foreignnationals.etranger.exception.ResourceNotFoundException;
import com.drag.foreignnationals.etranger.mapper.AddressMapper;
import com.drag.foreignnationals.etranger.mapper.PersonDetailMapper;
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
import java.util.Comparator;
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
    PersonDetailMapper personDetailMapper;
    @Autowired
    ResidencePermitRepository residencePermitRepository;
    @Autowired
    AddressMapper addressMapper;
    @Autowired
    AddressRepository addressRepository;
    @Autowired
    ResidencePermitMapper residencePermitMapper;

    @Override
    public PersonDetailDTO createPerson(PersonDetailDTO dto) {

        Address address = new Address();
        ResidencePermit residencePermit = new ResidencePermit();
        Person person = personDetailMapper.toPerson(dto);

        person = personRepository.save(person);
        if (dto.getCurrentAddress() != null) {
            address = addressMapper.toEntity(dto.getCurrentAddress());
            address.setPerson(person);
            addressRepository.save(address);
        }

        if (dto.getLastResidencePermit() != null){
            residencePermit = residencePermitMapper.toEntity(dto.getLastResidencePermit());
            residencePermit.setPerson(person);
            residencePermitRepository.save(residencePermit);

        }

        return personDetailMapper.toPersonDetailDto(person, address, residencePermit);




    }

    @Override
    public PersonDetailDTO getPerson(Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with ID " + id));
        // --- Pre-process current address ---
        Address currentAddress = person.getAddresses().stream()
                .filter(Address::isCurrent)              // assuming you have a boolean flag
                .findFirst()
                .orElse(null);

        // --- Pre-process last residence permit ---
        ResidencePermit lastPermit = person.getResidencePermits().stream()
                .max(Comparator.comparing(ResidencePermit::getDateOfIssue)) // latest by issueDate
                .orElse(null);
        return personDetailMapper.toPersonDetailDto(person, currentAddress, lastPermit);
    }

    @Override
    public List<PersonDTO> getAllPersons() {
        return personRepository.findAll()
                .stream()
                .map(personMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PersonDetailDTO updatePerson(Long id, PersonDetailDTO dto) {

        Address address = new Address();
        ResidencePermit residencePermit = new ResidencePermit();
        Person existing = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with ID " + id));
        existing.setFirstName(dto.getFirstName());
        existing.setLastName(dto.getLastName());
        existing.setDateOfBirth(dto.getDateOfBirth());
        existing.setGender(dto.getGender());
        existing = personRepository.save(existing);

        if (dto.getCurrentAddress() != null) {
            address = addressMapper.toEntity(dto.getCurrentAddress());
            address.setPerson(existing);
            addressRepository.save(address);
        }

        if (dto.getLastResidencePermit() != null){
            residencePermit = residencePermitMapper.toEntity(dto.getLastResidencePermit());
            residencePermit.setPerson(existing);
            residencePermitRepository.save(residencePermit);

        }

        return personDetailMapper.toPersonDetailDto(personRepository.save(existing));
    }

    @Override
    public void deletePerson(Long id) {
        if (!personRepository.existsById(id)) {
            throw new ResourceNotFoundException("Person not found with ID " + id);
        }
        personRepository.deleteById(id);
    }
}