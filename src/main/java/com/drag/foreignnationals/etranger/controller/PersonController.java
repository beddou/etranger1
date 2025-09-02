package com.drag.foreignnationals.etranger.controller;


import com.drag.foreignnationals.etranger.dto.PersonDetailDTO;
import com.drag.foreignnationals.etranger.service.PersonService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;
    private String personNotSaved = "Person not saved";
    private String personNotFound = "Person not found";

    @PostMapping
    public ResponseEntity<PersonDetailDTO> create(@RequestBody PersonDetailDTO dto) {
        try {
            PersonDetailDTO person = personService.create(dto);
            return new ResponseEntity<>(person, HttpStatus.CREATED);
        } catch (Exception e) {
            throw new NoEntityAddedException(assuranceNotSaved);
        }
        return ResponseEntity.ok(personService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonDetailDTO> get(@PathVariable @Min(value = 1, message = "Id must be positive") Long id) {
        return ResponseEntity.ok(personService.get(id));
    }



    @PutMapping("/{id}")
    public ResponseEntity<PersonDetailDTO> update(@PathVariable Long id, @RequestBody PersonDetailDTO dto) {
        return ResponseEntity.ok(personService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        personService.delete(id);
        return ResponseEntity.noContent().build();
    }
}