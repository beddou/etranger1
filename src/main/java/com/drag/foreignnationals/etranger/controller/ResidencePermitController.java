package com.drag.foreignnationals.etranger.controller;

import com.drag.foreignnationals.etranger.dto.ResidencePermitDTO;
import com.drag.foreignnationals.etranger.repository.PersonRepository;
import com.drag.foreignnationals.etranger.service.ResidencePermitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/persons/{personId}/residence-permits")
@RequiredArgsConstructor
@Validated
public class ResidencePermitController {

    @Autowired
    private ResidencePermitService residencePermitService;
    @Autowired
    private PersonRepository personRepository;

    @PostMapping
    public ResponseEntity<ResidencePermitDTO> create(@PathVariable Long personId, @Valid @RequestBody ResidencePermitDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(residencePermitService.create(personId, dto));
    }

    @PutMapping("/{permitId}")
    public ResponseEntity<ResidencePermitDTO> update(@PathVariable Long personId, @Valid @PathVariable Long permitId, @RequestBody ResidencePermitDTO dto) {
        return ResponseEntity.ok(residencePermitService.update(personId, permitId, dto));
    }

    @DeleteMapping("/{permitId}")
    public ResponseEntity<Void> delete(@PathVariable Long personId, @PathVariable Long permitId) {
        residencePermitService.delete(personId, permitId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ResidencePermitDTO>> getByPerson(@PathVariable Long personId) {
        return ResponseEntity.ok(residencePermitService.getByPersonId(personId));
    }

    @GetMapping("/{permitId}")
    public ResponseEntity<ResidencePermitDTO> getById(
            @PathVariable Long personId,
            @PathVariable Long permitId) {
        return ResponseEntity.ok(residencePermitService.getById(personId, permitId));
    }
}
