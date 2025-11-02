package com.drag.foreignnationals.etranger.service.impl;

import com.drag.foreignnationals.etranger.dto.SituationDTO;
import com.drag.foreignnationals.etranger.entity.Situation;
import com.drag.foreignnationals.etranger.exception.BusinessException;
import com.drag.foreignnationals.etranger.exception.ErrorCode;
import com.drag.foreignnationals.etranger.mapper.SituationMapper;
import com.drag.foreignnationals.etranger.repository.SituationRepository;
import com.drag.foreignnationals.etranger.service.SituationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SituationServiceImpl implements SituationService {

    private final SituationRepository repository;
    private final SituationMapper mapper;

    @Override
    public SituationDTO create(SituationDTO dto) {
        return mapper.toDTO(repository.save(mapper.toEntity(dto)));
    }

    @Override
    public SituationDTO get(Long id) {
        Situation entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ENTITY_NOT_FOUND, "Situation not found"));
        return mapper.toDTO(entity);
    }

    @Override
    public List<SituationDTO> getAll() {
        return repository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SituationDTO update(Long id, SituationDTO dto) {
        Situation entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ENTITY_NOT_FOUND, "Situation not found"));

        entity.setType(dto.getType());
        entity.setDate(dto.getDate());
        entity.setComment(dto.getComment());
        return mapper.toDTO(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new BusinessException(
                    ErrorCode.ENTITY_NOT_FOUND, "Situation not found");
        }
        repository.deleteById(id);
    }
}