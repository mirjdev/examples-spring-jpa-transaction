package com.mirjdev.examplesspring.controller;

import com.mirjdev.examplesspring.service.TaskHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Positive;

@Slf4j
@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {
    private final TaskHandlerService taskHandlerService;

    @SneakyThrows
    @GetMapping("/generate/{count}")
    public void generate(@PathVariable @Positive int count) {
        taskHandlerService.generateTask(count);
    }

    @SneakyThrows
    @GetMapping("/generate/start")
    public void generate() {
        taskHandlerService.handle();
    }
}
