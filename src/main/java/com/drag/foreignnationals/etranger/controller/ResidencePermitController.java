package com.drag.foreignnationals.etranger.controller;

import com.drag.foreignnationals.etranger.dto.ResidencePermitDTO;
import com.drag.foreignnationals.etranger.repository.PersonRepository;
import com.drag.foreignnationals.etranger.service.ResidencePermitService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/residence-permits")
@RequiredArgsConstructor
public class ResidencePermitController {

    @Autowired
    private ResidencePermitService residencePermitService;
    @Autowired
    private PersonRepository personRepository;

    @PostMapping("/person/{personId}")
    public ResponseEntity<ResidencePermitDTO> create( @RequestBody ResidencePermitDTO dto) {

        return ResponseEntity.ok(residencePermitService.create( dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResidencePermitDTO> update(@PathVariable Long id, @RequestBody ResidencePermitDTO dto) {
        return ResponseEntity.ok(residencePermitService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        residencePermitService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/person/{personId}")
    public ResponseEntity<List<ResidencePermitDTO>> getByPerson(@PathVariable Long personId) {
        return ResponseEntity.ok(residencePermitService.getByPersonId(personId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResidencePermitDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(residencePermitService.getById(id));
    }
}
