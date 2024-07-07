package com.mirjdev.examplesspring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class Scheduler {

    private final TaskHandlerService taskHandlerService;

    @Scheduled(fixedDelayString = "${handling.tick.rate:1000}", initialDelay = 30000)
    public void handleExpiredTimers() {
        // TODO добавить GracefulShutdown
        taskHandlerService.handle();
    }
}
