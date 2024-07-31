package com.mirjdev.examplesspring.service;

import com.mirjdev.examplesspring.entity.Driver;

import java.io.IOException;

public interface IsolationLevelService {

    Driver generateFirstDriver();

    Driver findFirstDriver();

    void updateAllThrowDivisionByZeroTrRequired();

    void updateAllThrowDivisionByZeroTrRequiresNew();

    void updateAllThrowCheckedException() throws IOException;

    Driver findFirstDriverRR();

    Driver saveRRAndMandatory(Driver driver);

    Driver updateDriver(Driver driver);
}
