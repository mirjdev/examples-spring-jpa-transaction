package com.mirjdev.examplesspring.service.step.impl;

import com.mirjdev.examplesspring.entity.State;
import com.mirjdev.examplesspring.entity.Task;
import com.mirjdev.examplesspring.entity.repository.TaskRepository;
import com.mirjdev.examplesspring.service.step.Step;
import com.mirjdev.examplesspring.service.step.StepEnum;
import com.mirjdev.examplesspring.service.step.Steper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Slf4j
@Service
public class SteperImpl implements Steper {

    //StepConfig пример как собрать мапу из бинов по типу
    private final Map<StepEnum, Step> mapSteps;
    private final TaskRepository taskRepository;

    //    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = Exception.class) // так как транзакция будет выполняться долго, забьется пул
    @Transactional(propagation = Propagation.NEVER)
    @Override
    public CompletableFuture<Task> run(Long taskId) {
        log.info("Task id: {}", taskId);
        Task processedTask = taskRepository.findTaskByIdWithoutOptLock(taskId);
        log.info("Processed task: {}", processedTask);
        mapSteps.get(StepEnum.LOG).execute(processedTask); // Такие логи могут забить пул отдельными транзакциями
        processedTask.setState(State.COMPLETE);  // данный переход не сохраниться, так как нет транзакции
        Task task = mapSteps.get(StepEnum.COMPLETE).execute(processedTask); // тут эмуляция долгого выполнения и перевод в COMPLETE
        mapSteps.get(StepEnum.LOG).execute(processedTask);

        CompletableFuture<Task> completableFuture = new CompletableFuture<>();
        completableFuture.complete(task);
        return completableFuture;
    }


//    @Transactional(propagation = Propagation.NEVER)
//    @Override
//    public CompletableFuture<Void> run(Long task) {
//        log.info("Task id: {}", task);
//        Task processedTask = taskRepository.findTaskByIdWithoutOptLock(task);
//        log.info("Processed task: {}", processedTask);
//        // mapSteps.get(StepEnum.LOG).execute(processedTask);
////        try {
//            mapSteps.get(StepEnum.COMPLETE).execute(processedTask);
////        } catch (RuntimeException e) {
////            log.error(e.getMessage());
////        }
//        return CompletableFuture.allOf();
//    }
}
