package com.mirjdev.examplesspring.controller;

import com.mirjdev.examplesspring.dto.DriverDto;
import com.mirjdev.examplesspring.mapper.DriverMapper;
import com.mirjdev.examplesspring.service.IsolationLevelFacade;
import com.mirjdev.examplesspring.service.IsolationLevelService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/api/isolation-level")
@RequiredArgsConstructor
public class IsolationLevelController {

    private final IsolationLevelService isolationLevelService;
    private final IsolationLevelFacade isolationLevelFacade;
    private final DriverMapper driverMapper;
    private final ExecutorService pool = Executors.newCachedThreadPool();

    @SneakyThrows
    @GetMapping("/generate")
    public ResponseEntity<DriverDto> generate() {
        return ResponseEntity.ok(driverMapper.toDto(isolationLevelService.generateFirstDriver()));
    }

    @SneakyThrows
    @GetMapping("/test-rr")
    public String testRR() {
        Future<String> submit = pool.submit(isolationLevelFacade::testRR);
        pool.execute(isolationLevelFacade::updateFirstDriver);
        //pool.execute(isolationLevelFacade::updateAllDr);
        return submit.get();
    }
}
