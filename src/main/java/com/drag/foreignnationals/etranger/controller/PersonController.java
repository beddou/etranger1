package com.drag.foreignnationals.etranger.controller;


import com.drag.foreignnationals.etranger.dto.*;

import com.drag.foreignnationals.etranger.service.PersonService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonController {


    private final PersonService personService;


    @GetMapping
    public ResponseEntity<Page<PersonDTO>> getAll(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Page<PersonDTO> result = personService.search(keyword, page, size, sortBy, direction);
        return ResponseEntity.ok(result);
    }


    @PostMapping
    public ResponseEntity<PersonDetailDTO> create(@Valid @RequestBody PersonCreateDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(personService.create(dto));

    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonDetailDTO> get(@PathVariable @Min(value = 1, message = "Id must be positive") Long id) {
        return ResponseEntity.ok(personService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonDetailDTO> update(@Valid @PathVariable Long id, @Valid @RequestBody PersonUpdateDTO dto) {
        return ResponseEntity.ok(personService.update(id, dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PersonDetailDTO> patch(
            @PathVariable Long id,
            @Valid @RequestBody PersonPatchDTO dto) {
        return ResponseEntity.ok(personService.patch(id, dto));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        personService.delete(id);
        return ResponseEntity.noContent().build();
    }
}