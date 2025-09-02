package com.drag.foreignnationals.etranger.service.impl;

import com.drag.foreignnationals.etranger.dto.ResidencePermitDTO;
import com.drag.foreignnationals.etranger.entity.Person;
import com.drag.foreignnationals.etranger.entity.ResidencePermit;
import com.drag.foreignnationals.etranger.mapper.ResidencePermitMapper;
import com.drag.foreignnationals.etranger.repository.PersonRepository;
import com.drag.foreignnationals.etranger.repository.ResidencePermitRepository;
import com.drag.foreignnationals.etranger.service.ResidencePermitService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResidencePermitServiceImpl implements ResidencePermitService {

    @Autowired
    ResidencePermitRepository permitRepository;
    @Autowired
    PersonRepository personRepository;
    @Autowired
    ResidencePermitMapper permitMapper;


    public ResidencePermitDTO create(ResidencePermitDTO dto) {
        ResidencePermit permit = permitMapper.toEntity(dto);
        long id = dto.getPerson().getId();
        if (id>0){
            Person person = personRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Person not found with ID " + id));
            permit.setPerson(person);
            return permitMapper.toDTO(permitRepository.save(permit));
        }else throw new ResourceNotFoundException("Person not found");

    }


    public ResidencePermitDTO update(Long id, ResidencePermitDTO dto) {
        ResidencePermit existing = permitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Residence permit not found"));


        existing.setDateOfIssue(dto.getDateOfIssue());
        existing.setDurationInMonths(dto.getDurationInMonths());

        return permitMapper.toDTO(permitRepository.save(existing));
    }


    public void delete(Long id) {
        if (!permitRepository.existsById(id)) {
            throw new ResourceNotFoundException("Residence permit not found");
        }
        permitRepository.deleteById(id);
    }


    public List<ResidencePermitDTO> getByPersonId(Long personId) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found"));

        return person.getResidencePermits().stream()
                .map(permitMapper::toDTO)
                .collect(Collectors.toList());
    }


    public ResidencePermitDTO getById(Long id) {
        ResidencePermit permit = permitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Residence permit not found"));
        return permitMapper.toDTO(permit);
    }
}