package com.mirjdev.examplesspring.service.step.impl;

import com.mirjdev.examplesspring.entity.State;
import com.mirjdev.examplesspring.entity.Task;
import com.mirjdev.examplesspring.entity.repository.TaskRepository;
import com.mirjdev.examplesspring.service.step.Step;
import com.mirjdev.examplesspring.service.step.StepEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Order(1)
@Slf4j
@RequiredArgsConstructor
@Service
public class TaskStepInProgress implements Step {

    private final TaskRepository taskRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Task execute(Task task) {
        if (task.getAttempt() >= 5) {
            task.setState(State.ERROR);
        } else {
            task.setState(State.IN_PROGRESS);
        }
        taskRepository.saveAndFlush(task);
        return task;
    }

    @Override
    public StepEnum getType() {
        return StepEnum.IN_PROGRESS;
    }
}
