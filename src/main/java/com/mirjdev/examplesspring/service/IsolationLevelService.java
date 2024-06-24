package com.mirjdev.examplesspring.service;

import com.mirjdev.examplesspring.entity.Driver;

public interface IsolationLevelService {

    Driver generateFirstDriver();

    Driver findFirstDriver();

    void updateAll();

    Driver findFirstDriverRR();

    Driver saveRRAndMandatory(Driver driver);

    Driver updateDriver(Driver driver);
}
