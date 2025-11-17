package com.drag.foreignnationals.etranger.controller;


import com.drag.foreignnationals.etranger.dto.CommuneDTO;
import com.drag.foreignnationals.etranger.dto.NationalityDTO;
import com.drag.foreignnationals.etranger.service.CommuneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/communes")
@RequiredArgsConstructor
public class CommuneController {

    private final CommuneService communeService;

    @GetMapping
    public ResponseEntity<List<CommuneDTO>> getAll() {
        return ResponseEntity.ok(communeService.getAll());
    }
}
