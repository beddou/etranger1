package com.drag.foreignnationals.etranger.controller;

import com.drag.foreignnationals.etranger.dto.ResidencePermitDTO;
import com.drag.foreignnationals.etranger.service.ResidencePermitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/residence-permits")
@RequiredArgsConstructor
public class ResidencePermitController {

    private final ResidencePermitService service;

    @PostMapping
    public ResponseEntity<ResidencePermitDTO> create(@RequestBody ResidencePermitDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResidencePermitDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<List<ResidencePermitDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResidencePermitDTO> update(@PathVariable Long id, @RequestBody ResidencePermitDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
