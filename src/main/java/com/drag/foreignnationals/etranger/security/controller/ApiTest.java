package com.drag.foreignnationals.etranger.security.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController

@RequiredArgsConstructor
@Validated

public class ApiTest {
    @GetMapping("/api/test")
    public String secured() {
        return "secured ok";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/test")
    public String adminOnly() {
        return "admin ok";
    }
}
