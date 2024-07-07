package com.mirjdev.examplesspring.service.step.impl;

import com.mirjdev.examplesspring.entity.Task;
import com.mirjdev.examplesspring.service.step.Steper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
public class SteperAsync {

    private final Steper steper;

    @Async("handlerTaskExecutor")
    public CompletableFuture<Task> run(Long task) {
        return steper.run(task);
    }

}
