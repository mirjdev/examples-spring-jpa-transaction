package com.mirjdev.examplesspring.service.step.impl;

import com.mirjdev.examplesspring.aop.TransactionMonitoring;
import com.mirjdev.examplesspring.entity.Task;
import com.mirjdev.examplesspring.entity.TaskLog;
import com.mirjdev.examplesspring.entity.repository.TaskLogRepository;
import com.mirjdev.examplesspring.service.step.Step;
import com.mirjdev.examplesspring.service.step.StepEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Order(2)
@Slf4j
@RequiredArgsConstructor
@Service
public class TaskStepLog implements Step {

    private final TaskLogRepository taskLogRepository;

    @TransactionMonitoring(level = Level.INFO)
    // Такие логи могут забить пул отдельными транзакциями
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = RuntimeException.class)
    @Override
    public Task execute(Task task) {
        String logText = String.format("id: %s, state: %s, thread: %s", task.getId(), task.getState(), Thread.currentThread().getName());
        taskLogRepository.saveAndFlush(TaskLog.builder().log(logText).task(task).build());
        log.info(logText);
        return task;
    }

    @Override
    public StepEnum getType() {
        return StepEnum.LOG;
    }
}
