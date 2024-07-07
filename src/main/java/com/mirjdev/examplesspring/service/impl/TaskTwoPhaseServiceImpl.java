package com.mirjdev.examplesspring.service.impl;

import com.mirjdev.examplesspring.entity.Task;
import com.mirjdev.examplesspring.entity.repository.TaskRepository;
import com.mirjdev.examplesspring.service.TaskTwoPhaseService;
import com.mirjdev.examplesspring.service.step.Step;
import com.mirjdev.examplesspring.service.step.StepEnum;
import com.mirjdev.examplesspring.service.step.Steper;
import com.mirjdev.examplesspring.service.step.impl.SteperAsync;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class TaskTwoPhaseServiceImpl implements TaskTwoPhaseService {

    private final Steper steper;
    private final SteperAsync steperAsync;
    private final Map<StepEnum, Step> mapSteps;
    private final TaskRepository taskRepository;

    private final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public List<Long> runPhaseOne(int batch) {
        List<Task> tasks = taskRepository.findTasks(batch);
        tasks.forEach(task -> {
                    task.setAttempt(task.getAttempt() + 1);
                    mapSteps.get(StepEnum.LOG).execute(task); //плохая идея логировать только в базу
                    mapSteps.get(StepEnum.IN_PROGRESS).execute(task);
                }
        );
        return tasks.stream().map(Task::getId).collect(Collectors.toList());
    }

    @Transactional(propagation = Propagation.NEVER)
    @Override
    public void runPhaseTwo(List<Long> tasks) {
        List<Future<CompletableFuture<Task>>> futures = tasks.stream()
                .map(id -> cachedThreadPool.submit(() -> steper.run(id)))
                .collect(Collectors.toList());

        for (Future<CompletableFuture<Task>> future : futures) {
            try {
                Task task = future.get()
                        .exceptionally(throwable -> null)
                        .get();
                log.info("Task future completed: {}", task);
            } catch (InterruptedException e) {
                log.info("InterruptedException: {}", e.getMessage());
            } catch (ExecutionException e) {
                log.error("Failed execution task: {}, {}", e, future);
            }
        }
        //Вариант с @Async
//        List<CompletableFuture<Void>> futures = tasks.stream().map(steperAsync::run).collect(Collectors.toList());
//        for (CompletableFuture<Void> future : futures) {
//            try {
//                Void join = future.join();
//                System.out.println("join = " + join);
//            } catch (CompletionException e) {
//                log.error("Failed ", e);
//            }
//        }
    }
}
