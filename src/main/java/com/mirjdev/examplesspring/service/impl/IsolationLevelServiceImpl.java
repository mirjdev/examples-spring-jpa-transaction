package com.mirjdev.examplesspring.service.impl;

import com.mirjdev.examplesspring.entity.Driver;
import com.mirjdev.examplesspring.entity.repository.DriverRepository;
import com.mirjdev.examplesspring.service.IsolationLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IsolationLevelServiceImpl implements IsolationLevelService {

    private final DriverRepository driverRepository;

    @Override
    public Driver generateFirstDriver() {
        Optional<Driver> driverOptional = driverRepository.findById(1L);
        if (driverOptional.isEmpty()) {
            driverRepository.saveAndFlush(
                    Driver.builder()
                            .id(1L)
                            .fio("fio_ver1")
                            .driverLicense("A1")
                            .build()
            );
        }
        return driverRepository.getById(1L);
    }

    @Override
    public Driver findFirstDriver() {
        return driverRepository.getById(1L);
    }

    @Override
    public void updateAll() {
        driverRepository.updateAllDr();
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
    @Override
    public Driver findFirstDriverRR() {
        return driverRepository.getById(1L);
    }

    @Transactional(propagation = Propagation.MANDATORY, isolation = Isolation.REPEATABLE_READ)
    @Override
    public Driver saveRRAndMandatory(Driver driver) {
        return driverRepository.saveAndFlush(driver);
    }

    @Override
    public Driver updateDriver(Driver driver) {
        return driverRepository.saveAndFlush(driver);
    }
}
