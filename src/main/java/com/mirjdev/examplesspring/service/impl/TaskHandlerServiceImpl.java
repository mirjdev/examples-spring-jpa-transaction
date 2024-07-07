package com.mirjdev.examplesspring.service.impl;

import com.mirjdev.examplesspring.entity.State;
import com.mirjdev.examplesspring.entity.Task;
import com.mirjdev.examplesspring.entity.TaskType;
import com.mirjdev.examplesspring.entity.repository.TaskRepository;
import com.mirjdev.examplesspring.service.TaskHandlerService;
import com.mirjdev.examplesspring.service.TaskTwoPhaseService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
@Service
public class TaskHandlerServiceImpl implements TaskHandlerService {

    @Value("${handlers.batch-size:100}")
    public int batchSize;

    private final TaskRepository taskRepository;
    private final TaskTwoPhaseService taskTwoPhaseService;

    final EasyRandomParameters parameters = new EasyRandomParameters()
            .excludeField(field -> field.getName().equals("id"))
            .excludeField(field -> field.getName().equals("completeDt"))
            .excludeField(field -> field.getName().equals("logs"))
            .randomize(field -> field.getName().equals("version"), () -> 0L)
            .randomize(field -> field.getName().equals("scheduledDt"), LocalDateTime::now)
            .randomize(field -> field.getName().equals("state"), State.SCHEDULED::name)
            .randomize(field -> field.getName().equals("taskType"), TaskType.DO_SOMETHING::name);
    final EasyRandom random = new EasyRandom(parameters);

    @Transactional
    @Override
    public void generateTask(int count) {
        List<Task> tasks = random.objects(Task.class, count).collect(toList());
        taskRepository.saveAll(tasks);
    }

    @SneakyThrows
    @Transactional(propagation = Propagation.NEVER)
    @Override
    public void handle() {
        taskRepository.reScheduleTasks();
        List<Long> tasks = taskTwoPhaseService.runPhaseOne(batchSize);
        log.info("{} tasks in progress", tasks.size());
        taskTwoPhaseService.runPhaseTwo(tasks);
    }
}