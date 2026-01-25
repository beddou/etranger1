package com.drag.foreignnationals.etranger.controller;


import com.drag.foreignnationals.etranger.dto.*;
import com.drag.foreignnationals.etranger.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/persons/{idPerson}/addresses")
@RequiredArgsConstructor
public class AddressController {


    private final  AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressDTO> add(@Valid @PathVariable Long idPerson, @Valid @RequestBody AddressCreateDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.add(idPerson, dto));

    }


    @GetMapping
    public ResponseEntity<List<AddressDTO>> getAll(@Valid @PathVariable Long idPerson) {
        return ResponseEntity.ok(addressService.getAllByPerson(idPerson));
    }
}
