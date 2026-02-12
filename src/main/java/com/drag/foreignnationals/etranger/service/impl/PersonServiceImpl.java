package com.drag.foreignnationals.etranger.service.impl;

import com.drag.foreignnationals.etranger.dto.*;
import com.drag.foreignnationals.etranger.entity.Address;
import com.drag.foreignnationals.etranger.entity.Commune;
import com.drag.foreignnationals.etranger.entity.Nationality;
import com.drag.foreignnationals.etranger.entity.Person;
import com.drag.foreignnationals.etranger.exception.BusinessException;
import com.drag.foreignnationals.etranger.exception.ErrorCode;
import com.drag.foreignnationals.etranger.mapper.AddressMapper;
import com.drag.foreignnationals.etranger.mapper.PersonMapper;
import com.drag.foreignnationals.etranger.repository.*;
import com.drag.foreignnationals.etranger.service.PersonService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;


@Validated
@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    @Autowired
    PersonRepository personRepository;
    @Autowired
    PersonMapper personMapper;

    @Autowired
    AddressMapper addressMapper;
    @Autowired
    AddressRepository addressRepository;
    @Autowired
    NationalityRepository nationalityRepository;
    @Autowired
    SituationRepository situationRepository;
    @Autowired
    CommuneRepository communeRepository;


    @Override
    @Transactional(readOnly = true)
    public Page<PersonDTO> search(String keyword, int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Person> personPage;
        if (keyword == null || keyword.trim().isEmpty()) {
            personPage = personRepository.findAll(pageable);
        } else {
            personPage = personRepository.search(keyword.trim(), pageable);
        }

        return personPage.map(personMapper::toPersonDto);
    }

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
        // (mandatory commune if address exists)
        if (dto.getCurrentAddress() != null) {

            Address address = addressMapper.toEntity(dto.getCurrentAddress());

            Long communeId = dto.getCurrentAddress().getCommuneId();

            if (communeId == null) {
                throw new BusinessException(
                        ErrorCode.VALIDATION_ERROR, "Commune is required when address is provided");
            }

            Commune commune = communeRepository.findById(communeId)
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.ENTITY_NOT_FOUND, "Commune not found"));
            // set relationships
            address.setCommune(commune);
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
    @Transactional(readOnly = true)
    public PersonDetailDTO getById(Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "Person not found with ID " + id));

        return personMapper.toPersonDetailDto(person);
    }

    @Override

    @Transactional
    public PersonDetailDTO update(Long id, PersonUpdateDTO dto) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "Person not found with ID " + id));

        // Update Person scalar fields
        personMapper.updateEntityFromDto(dto, person);

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

        return personMapper.toPersonDetailDto(person);

    }

    @Transactional
    public PersonDetailDTO patch(Long id, PersonPatchDTO dto) {

        Person person = personRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "Person not found with ID " + id
                ));

        // 1. Scalar fields
        dto.getFirstName().ifPresent(person::setFirstName);
        dto.getLastName().ifPresent(person::setLastName);
        dto.getDateOfBirth().ifPresent(person::setDateOfBirth);
        dto.getGender().ifPresent(person::setGender);

        // 2. Nationality
        dto.getNationalityId().ifPresent(natId -> {
            Nationality nat = nationalityRepository.findById(natId)
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.ENTITY_NOT_FOUND,
                            "Nationality not found with ID " + natId
                    ));
            person.setNationality(nat);
        });

        // 3. Situation
        dto.getSituationId()
                .flatMap(situationRepository::findById)
                .ifPresent(person::setSituation);
        /*dto.getSituationId().ifPresent(sitId ->
                situationRepository.findById(sitId).ifPresent(person::setSituation)
        );*/

        // 4. Current address
        dto.getCurrentAddress().ifPresent(addressDTO ->
                updateCurrentAddress(person, addressDTO)
        );

        Person saved = personRepository.save(person);
        return personMapper.toPersonDetailDto(saved);
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