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

    @GetMapping
    public ResponseEntity<List<NationalityDTO>> getAll() {

        return ResponseEntity.ok(service.getAll());
    }


}
