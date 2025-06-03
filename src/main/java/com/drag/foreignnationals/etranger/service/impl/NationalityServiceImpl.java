package com.drag.foreignnationals.etranger.service.impl;

import com.drag.foreignnationals.etranger.dto.NationalityDTO;
import com.drag.foreignnationals.etranger.entity.Nationality;
import com.drag.foreignnationals.etranger.exception.ResourceNotFoundException;
import com.drag.foreignnationals.etranger.mapper.NationalityMapper;
import com.drag.foreignnationals.etranger.repository.NationalityRepository;
import com.drag.foreignnationals.etranger.service.NationalityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NationalityServiceImpl implements NationalityService {

    private final NationalityRepository repository;
    private final NationalityMapper mapper;

    @Override
    public NationalityDTO create(NationalityDTO dto) {
        return mapper.toDTO(repository.save(mapper.toEntity(dto)));
    }

    @Override
    public NationalityDTO get(Long id) {
        Nationality entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nationality not found"));
        return mapper.toDTO(entity);
    }

    @Override
    public List<NationalityDTO> getAll() {
        return repository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public NationalityDTO update(Long id, NationalityDTO dto) {
        Nationality nationality = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nationality not found"));
        nationality.setCountry(dto.getCountry());
        return mapper.toDTO(repository.save(nationality));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Nationality not found");
        }
        repository.deleteById(id);
    }
}