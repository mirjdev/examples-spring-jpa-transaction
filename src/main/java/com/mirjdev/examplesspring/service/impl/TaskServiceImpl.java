package com.mirjdev.examplesspring.service.impl;

import com.mirjdev.examplesspring.entity.State;
import com.mirjdev.examplesspring.entity.Task;
import com.mirjdev.examplesspring.entity.TaskType;
import com.mirjdev.examplesspring.entity.repository.TaskRepository;
import com.mirjdev.examplesspring.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class TaskServiceImpl implements TaskService {

    private static final int POOL_SIZE = 100;

    private final ExecutorService scheduledPool = Executors.newFixedThreadPool(POOL_SIZE);

    private final TaskRepository taskRepository;

    final EasyRandomParameters parameters = new EasyRandomParameters()
            .excludeField(field -> field.getName().equals("id"))
            .excludeField(field -> field.getName().equals("completeDt"))
            .randomize(field -> field.getName().equals("version"), () -> 0L)
            .randomize(field -> field.getName().equals("scheduledDt"), LocalDateTime::now)
            .randomize(field -> field.getName().equals("state"), State.SCHEDULED::name)
            .randomize(field -> field.getName().equals("taskType"), TaskType.DO_SOMETHING::name);
    final EasyRandom random = new EasyRandom(parameters);

    @Transactional
    @Override
    public void generateTask(int count) {
        List<Task> tasks = random.objects(Task.class, count).collect(Collectors.toList());
        taskRepository.saveAll(tasks);
    }

    @SneakyThrows
    @Transactional
    @Override
    public void startProcess() {
        for (int i = 0; i < POOL_SIZE; i++) {
            scheduledPool.execute(() -> {
                List<Task> tasks = taskRepository.findTasks(10);
                if (!tasks.isEmpty()) {
                    tasks.forEach(task -> {
                        task.setState(State.COMPLETE);
                        task.setCompleteDt(LocalDateTime.now());
                        taskRepository.save(task);
                        log.info("task completed: {}", task.getId());
                    });
                }
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
