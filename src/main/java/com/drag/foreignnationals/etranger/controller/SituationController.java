package com.drag.foreignnationals.etranger.controller;

import com.drag.foreignnationals.etranger.dto.SituationDTO;
import com.drag.foreignnationals.etranger.service.SituationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/situations")
@RequiredArgsConstructor
public class SituationController {

    private final SituationService service;

    @GetMapping("/{id}")
    public ResponseEntity<SituationDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<List<SituationDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }


}