package com.drag.foreignnationals.etranger.service.impl;

import com.drag.foreignnationals.etranger.dto.ResidencePermitDTO;
import com.drag.foreignnationals.etranger.entity.Address;
import com.drag.foreignnationals.etranger.entity.Person;
import com.drag.foreignnationals.etranger.entity.ResidencePermit;
import com.drag.foreignnationals.etranger.exception.BusinessException;
import com.drag.foreignnationals.etranger.exception.ErrorCode;
import com.drag.foreignnationals.etranger.mapper.ResidencePermitMapper;
import com.drag.foreignnationals.etranger.repository.PersonRepository;
import com.drag.foreignnationals.etranger.repository.ResidencePermitRepository;
import com.drag.foreignnationals.etranger.service.ResidencePermitService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ResidencePermitServiceImpl implements ResidencePermitService {

    @Autowired
    ResidencePermitRepository permitRepository;
    @Autowired
    PersonRepository personRepository;
    @Autowired
    ResidencePermitMapper permitMapper;


    @Transactional
    public ResidencePermitDTO create(ResidencePermitDTO dto) {
        if (dto.getPersonId() == null ) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "Person information is required to create a residence permit."
            );
        }

        long personId = dto.getPersonId();

            Person person = personRepository.findById(personId)
                    .orElseThrow(() ->new BusinessException(
                            ErrorCode.ENTITY_NOT_FOUND, "Person not found with ID " + personId));


        // Get active residence permit
        ResidencePermit activeResidencePermit = person.getActiveResidencePermit();
        if (activeResidencePermit != null) {
            activeResidencePermit.setActive(false);
            permitRepository.save(activeResidencePermit);
        }

        ResidencePermit permit = permitMapper.toEntity(dto);
        permit.setActive(true);
        person.addPermit(permit);
        ResidencePermit saved = permitRepository.save(permit);
            return permitMapper.toDTO(saved);


    }

    @Transactional
    public ResidencePermitDTO update(Long id, ResidencePermitDTO dto) {
        ResidencePermit existing = permitRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ENTITY_NOT_FOUND, "Residence permit not found with ID " + id));


        existing.setDateOfIssue(dto.getDateOfIssue());
        existing.setDurationInMonths(dto.getDurationInMonths());

        ResidencePermit updated = permitRepository.save(existing);
        return permitMapper.toDTO(permitRepository.save(updated));
    }

@Transactional
    public void delete(Long id) {

        ResidencePermit permit = permitRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "Residence permit not found with ID " + id
                ));

    permitRepository.delete(permit);

    }


    public List<ResidencePermitDTO> getByPersonId(Long personId) {

        return permitRepository.findByPersonId(personId).stream()
                .map(permitMapper::toDTO)
                .toList();
    }


    public ResidencePermitDTO getById(Long id) {
        ResidencePermit permit = permitRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ENTITY_NOT_FOUND, "Residence permit not found with ID " + id ));
        return permitMapper.toDTO(permit);
    }
}