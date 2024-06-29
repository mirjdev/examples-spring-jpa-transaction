package com.mirjdev.examplesspring.controller;

import com.mirjdev.examplesspring.service.ProcedureService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/procedure")
@RequiredArgsConstructor
public class ProcedureController {

    private final ProcedureService procedureService;

    @GetMapping("/generate/string/{length}")
    public String generateString(@PathVariable int length) {
        return procedureService.generateString(length);
    }
}
