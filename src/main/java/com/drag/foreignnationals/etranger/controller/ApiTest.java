package com.drag.foreignnationals.etranger.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController

@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class ApiTest {
    /*@GetMapping("/api/test")
    public String secured() {
        return "secured ok";
    }*/


    @GetMapping("/admin/test")
    public String adminOnly() {
        return "admin ok";
    }
}
