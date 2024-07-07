package com.mirjdev.examplesspring.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
@EnableAsync
@RequiredArgsConstructor
public class SchedulingConfiguration implements SchedulingConfigurer {

    @Value("${handlers.max-pool-size:100}")
    private int handlerMaxPoolSize;
    @Value("${handlers.max-core-pool-size:100}")
    private int handlerMaxCorePoolSize;
    @Value("${handlers.queue-capacity:100}")
    private int handlerQueueCapacity;

    @Value("${global.handler.max-pool-size:5}")
    private int globalHandlerMaxPoolSize;
    @Value("${handlers.wait-for-tasks-to-complete-on-shutdown:true}")
    private boolean waitForTasksToCompleteOnShutdown;
    @Value("${handlers.await-termination-seconds:10}")
    private int awaitTerminationSeconds;

    @Bean
    public TaskExecutor handlerTaskExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(handlerMaxPoolSize);
        executor.setCorePoolSize(handlerMaxCorePoolSize);
        executor.setQueueCapacity(handlerQueueCapacity);
        executor.setWaitForTasksToCompleteOnShutdown(waitForTasksToCompleteOnShutdown);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
        executor.setThreadNamePrefix("handler-");
        executor.setThreadGroupName("handler");
        return executor;
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setThreadNamePrefix("global-handler-");
        taskScheduler.setThreadGroupName("global-handler");
        taskScheduler.setPoolSize(globalHandlerMaxPoolSize);
        taskScheduler.setWaitForTasksToCompleteOnShutdown(waitForTasksToCompleteOnShutdown);
        taskScheduler.setAwaitTerminationSeconds(awaitTerminationSeconds);
        taskScheduler.initialize();
        return taskScheduler;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.setScheduler(taskScheduler());
    }
}
