package com.drag.foreignnationals.etranger.service.impl;

import com.drag.foreignnationals.etranger.dto.ResidencePermitDTO;
import com.drag.foreignnationals.etranger.entity.ResidencePermit;
import com.drag.foreignnationals.etranger.exception.ResourceNotFoundException;
import com.drag.foreignnationals.etranger.mapper.ResidencePermitMapper;
import com.drag.foreignnationals.etranger.repository.ResidencePermitRepository;
import com.drag.foreignnationals.etranger.service.ResidencePermitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResidencePermitServiceImpl implements ResidencePermitService {

    private final ResidencePermitRepository repository;
    private final ResidencePermitMapper mapper;

    @Override
    public ResidencePermitDTO create(ResidencePermitDTO dto) {
        return mapper.toDTO(repository.save(mapper.toEntity(dto)));
    }

    @Override
    public ResidencePermitDTO get(Long id) {
        ResidencePermit entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Residence permit not found"));
        return mapper.toDTO(entity);
    }

    @Override
    public List<ResidencePermitDTO> getAll() {
        return repository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ResidencePermitDTO update(Long id, ResidencePermitDTO dto) {
        ResidencePermit entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Residence permit not found"));
        entity.setDateOfIssue(dto.getDateOfIssue());
        entity.setDurationInMonths(dto.getDurationInMonths());
        return mapper.toDTO(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Residence permit not found");
        }
        repository.deleteById(id);
    }
}