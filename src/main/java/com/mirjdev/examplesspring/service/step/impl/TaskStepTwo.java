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

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Order(2)
@Slf4j
@RequiredArgsConstructor
@Service
public class TaskStepTwo implements Step {

    private final TaskRepository taskRepository;

    //Так как есть долгая логика, мы не хотим на долго занимать транзакцию, иначе забьем пул
    @Transactional(propagation = Propagation.NEVER)
    @Override
    public Task execute(Task task) {
        //ждем от 1 до 5 сек
//        int seconds = ThreadLocalRandom.current().nextInt(1, 5);
//        try {
//            TimeUnit.SECONDS.sleep(seconds);
//        } catch (InterruptedException e) {
//            log.warn(e.getMessage());
//        }
        task.setState(State.COMPLETE);
        task.setCompleteDt(LocalDateTime.now());
        //Эмуляция ошибки
        if (Math.IEEEremainder(task.getId(), 12) == 0) {
            throw new RuntimeException("Exception in execute task: {}" + task.getId());
        }
        if (Math.IEEEremainder(task.getId(), 13) == 0) {
            // throw constraint database error. ConstraintViolationException
            task.setEntityId(null);
        }
        taskRepository.saveAndFlush(task);
        return task;
    }

    @Override
    public StepEnum getType() {
        return StepEnum.COMPLETE;
    }
}
