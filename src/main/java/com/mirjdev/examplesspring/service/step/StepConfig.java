package com.mirjdev.examplesspring.service.step;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Configuration
public class StepConfig {
    @Bean
    public Map<StepEnum, Step> handlerTable(Set<? extends Step> steps) {
        Map<StepEnum, Step> stepMap = new HashMap<>();
        steps.forEach(step -> stepMap.put(step.getType(), step));
        return Collections.unmodifiableMap(stepMap);
    }
}
