package com.drag.foreignnationals.etranger.service.impl;


import com.drag.foreignnationals.etranger.dto.CommuneDTO;


import com.drag.foreignnationals.etranger.mapper.CommuneMapper;

import com.drag.foreignnationals.etranger.repository.CommuneRepository;

import com.drag.foreignnationals.etranger.service.CommuneService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommuneServiceImpl implements CommuneService {

    @Autowired
    private CommuneRepository repository;

    @Autowired
    private CommuneMapper mapper;

    @Override
    public List<CommuneDTO> getAll() {
        return repository.findAll().stream()
                .map(mapper::toDTO).toList();
    }
}
