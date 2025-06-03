package com.drag.foreignnationals.etranger.controller;

import com.drag.foreignnationals.etranger.dto.NationalityDTO;
import com.drag.foreignnationals.etranger.service.NationalityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nationalities")
@RequiredArgsConstructor
public class NationalityController {

    private final NationalityService service;

    @PostMapping
    public ResponseEntity<NationalityDTO> create(@RequestBody NationalityDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NationalityDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<List<NationalityDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<NationalityDTO> update(@PathVariable Long id, @RequestBody NationalityDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
