package com.mirjdev.examplesspring.service.step;

import com.mirjdev.examplesspring.entity.Task;

import java.util.concurrent.CompletableFuture;

public interface Steper {

    CompletableFuture<Task> run(Long task);
}
