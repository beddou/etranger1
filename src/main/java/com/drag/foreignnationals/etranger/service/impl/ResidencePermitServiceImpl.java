package com.drag.foreignnationals.etranger.service.impl;

import com.drag.foreignnationals.etranger.dto.ResidencePermitDTO;
import com.drag.foreignnationals.etranger.entity.Person;
import com.drag.foreignnationals.etranger.entity.ResidencePermit;
import com.drag.foreignnationals.etranger.exception.BusinessException;
import com.drag.foreignnationals.etranger.exception.ErrorCode;
import com.drag.foreignnationals.etranger.mapper.ResidencePermitMapper;
import com.drag.foreignnationals.etranger.repository.PersonRepository;
import com.drag.foreignnationals.etranger.repository.ResidencePermitRepository;
import com.drag.foreignnationals.etranger.service.ResidencePermitService;
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
    public ResidencePermitDTO create(Long personId, ResidencePermitDTO dto) {

        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new BusinessException(
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
        return permitMapper.toDTO(permitRepository.save(permit));


    }

    @Transactional
    public ResidencePermitDTO update(Long personId, Long permitId, ResidencePermitDTO dto) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "Person not found with ID " + personId
                ));

        ResidencePermit existing = permitRepository.findById(permitId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ENTITY_NOT_FOUND, "Residence permit not found with ID " + permitId));


        existing.setDateOfIssue(dto.getDateOfIssue());
        existing.setDurationInMonths(dto.getDurationInMonths());
        existing.setType(dto.getType());

        ResidencePermit updated = permitRepository.save(existing);
        return permitMapper.toDTO(permitRepository.save(updated));
    }

    @Transactional
    public void delete(Long personId, Long permitId) {

        ResidencePermit permit = permitRepository.findById(permitId)
                .filter(p -> p.getPerson().getId().equals(personId))
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "Residence permit not found for this person with ID " + permitId
                ));


        // Prevent deletion if permit is active
        if (permit.isActive()) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "Cannot delete active residence permit"
            );
        }

        permitRepository.delete(permit);

    }

    @Transactional
    public List<ResidencePermitDTO> getByPersonId(Long personId) {

        return permitRepository.findByPersonId(personId).stream()
                .map(permitMapper::toDTO)
                .toList();
    }

    @Transactional
    public ResidencePermitDTO getById(Long personId, Long permitId) {

        ResidencePermit permit = permitRepository.findById(permitId)
                .filter(p -> p.getPerson().getId().equals(personId))
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "Residence permit not found for this person with ID " + permitId
                ));

        return permitMapper.toDTO(permit);
    }
}