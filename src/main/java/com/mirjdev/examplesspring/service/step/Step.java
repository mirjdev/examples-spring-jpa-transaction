package com.mirjdev.examplesspring.service.step;

import com.mirjdev.examplesspring.entity.Task;

public interface Step {

    Task execute(Task task);

    StepEnum getType();
}
