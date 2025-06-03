package com.drag.foreignnationals.etranger.controller;

import com.drag.foreignnationals.etranger.dto.PersonDTO;
import com.drag.foreignnationals.etranger.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @PostMapping
    public ResponseEntity<PersonDTO> create(@RequestBody PersonDTO dto) {
        return ResponseEntity.ok(personService.createPerson(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(personService.getPerson(id));
    }

    @GetMapping
    public ResponseEntity<List<PersonDTO>> getAll() {
        return ResponseEntity.ok(personService.getAllPersons());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonDTO> update(@PathVariable Long id, @RequestBody PersonDTO dto) {
        return ResponseEntity.ok(personService.updatePerson(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        personService.deletePerson(id);
        return ResponseEntity.noContent().build();
    }
}