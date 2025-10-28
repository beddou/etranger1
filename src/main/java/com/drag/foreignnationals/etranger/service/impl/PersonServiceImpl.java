package com.drag.foreignnationals.etranger.service.impl;

import com.drag.foreignnationals.etranger.dto.AddressCreateDto;
import com.drag.foreignnationals.etranger.dto.PersonCreateDTO;
import com.drag.foreignnationals.etranger.dto.PersonDTO;
import com.drag.foreignnationals.etranger.dto.PersonDetailDTO;
import com.drag.foreignnationals.etranger.entity.Address;
import com.drag.foreignnationals.etranger.entity.Nationality;
import com.drag.foreignnationals.etranger.entity.Person;
import com.drag.foreignnationals.etranger.entity.ResidencePermit;
import com.drag.foreignnationals.etranger.exception.BusinessException;
import com.drag.foreignnationals.etranger.exception.ErrorCode;
import com.drag.foreignnationals.etranger.mapper.AddressMapper;
import com.drag.foreignnationals.etranger.mapper.PersonMapper;
import com.drag.foreignnationals.etranger.mapper.ResidencePermitMapper;
import com.drag.foreignnationals.etranger.repository.*;
import com.drag.foreignnationals.etranger.service.PersonService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;

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
    PersonMapper personDetailMapper;
    @Autowired
    ResidencePermitRepository residencePermitRepository;
    @Autowired
    AddressMapper addressMapper;
    @Autowired
    AddressRepository addressRepository;
    @Autowired
    ResidencePermitMapper residencePermitMapper;
    @Autowired
    NationalityRepository nationalityRepository;
    @Autowired
    SituationRepository situationRepository;
    @Autowired
    CommuneRepository communeRepository;

    @Override
    @Transactional
    public PersonDetailDTO create(PersonCreateDTO dto) {

        // 1. map basic fields
        Person person = personMapper.toEntity(dto);

            // 2. set nationality (required)
            Nationality nat = nationalityRepository.findById(dto.getNationalityId())
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.ENTITY_NOT_FOUND, "Nationality not found"));
            person.setNationality(nat);

            // 3. set situation if provided (optional)
            if (dto.getSituationId() != null) {
                situationRepository.findById(dto.getSituationId())
                        .ifPresent(person::setSituation);
            }

            // 5. handle current address (if provided)
            if (dto.getCurrentAddress() != null) {
                Address address = addressMapper.toEntity(dto.getCurrentAddress());
                // set commune if provided
                if (dto.getCurrentAddress().getCommuneId() != null) {
                    communeRepository.findById(dto.getCurrentAddress().getCommuneId())
                            .ifPresent(address::setCommune);
                }
                // set relationships
                address.setPerson(person);
                address.setCurrent(true);

                // ensure person.addresses contains it
                person.getAddresses().add(address);// cascade saves it later

            }

            // 5. Persist everything in one transaction
            Person savedPerson = personRepository.save(person);

            // return detail dto
            return personMapper.toPersonDetailDto(savedPerson);




}

    @Override
    public PersonDetailDTO get(Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND,"Person not found with ID " + id));

        return personDetailMapper.toPersonDetailDto(person);
    }

    @Override
    @Transactional
    public List<PersonDTO> search(String keyword){
        return personRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(keyword, keyword)
                .stream()
                .map(personMapper::toPersonDto)
                .collect(Collectors.toList());

    }



    @Override

    @Transactional
    public PersonDetailDTO update(Long id, PersonCreateDTO dto) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND,"Person not found with ID " + id));

        // Update Person scalar fields
        personDetailMapper.updateEntityFromDto(dto, person);

        // update nationality if provided
        if (dto.getNationalityId() != null) {
            Nationality nat = nationalityRepository.findById(dto.getNationalityId())
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.ENTITY_NOT_FOUND, "Nationality not found"));
            person.setNationality(nat);
        }

        // update situation if provided
        if (dto.getSituationId() != null) {
            situationRepository.findById(dto.getSituationId())
                    .ifPresent(person::setSituation);
        }

        // --- Update current address ---
        if (dto.getCurrentAddress() != null) {
            updateCurrentAddress(person, dto.getCurrentAddress());
        }

        person = personRepository.save(person);

        return personDetailMapper.toPersonDetailDto(person);

    }


    @Override
    @Transactional
    public void delete(Long id) {
        if (!personRepository.existsById(id)) {
            throw new EntityNotFoundException("Person not found with ID " + id);
        }
        personRepository.deleteById(id);
    }


    //*****************************************
    ///  set current address
    private void updateCurrentAddress(Person person, AddressCreateDto addressDTO) {

        // Get current active address
        Address currentAddress = person.getCurrentAddress();

        if (currentAddress == null) {
            // No address yet → create new
            currentAddress = addressMapper.toEntity(addressDTO);
            currentAddress.setPerson(person);
            person.getAddresses().add(currentAddress);
        } else {
            // Update existing current address
            addressMapper.updateAddressFromDto(addressDTO, currentAddress);
        }

        // Update commune if provided
        if (addressDTO.getCommuneId() != null) {
            communeRepository.findById(addressDTO.getCommuneId())
                    .ifPresent(currentAddress::setCommune);
        }

        currentAddress.setCurrent(true);
        addressRepository.save(currentAddress);
    }
}