package com.mirjdev.examplesspring.service;

import java.util.List;

public interface TaskTwoPhaseService {

    List<Long> runPhaseOne(int batch);

    void runPhaseTwo(List<Long> tasks);

}
