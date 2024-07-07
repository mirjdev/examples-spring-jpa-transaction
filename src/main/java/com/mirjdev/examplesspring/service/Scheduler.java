package com.mirjdev.examplesspring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "task-handler", havingValue = "true", matchIfMissing = true)
@Service
public class Scheduler {

    private final TaskHandlerService taskHandlerService;

    @Scheduled(fixedDelayString = "${scheduler.tick.rate.task-handler:1000}", initialDelay = 30000)
    public void handleExpiredTimers() {
        // TODO добавить GracefulShutdown
        taskHandlerService.handle();
    }
}
