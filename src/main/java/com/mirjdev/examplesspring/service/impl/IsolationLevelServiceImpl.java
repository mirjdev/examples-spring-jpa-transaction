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
        Optional<Driver> driverOptional = driverRepository.findByIdNoLock(1L);
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

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateAllThrowDivisionByZeroTrRequired() {
        driverRepository.updateAllDrFioNative("fio_ver3_DivisionByZero");
        System.out.println("Division by zero  = " + 1/0);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void updateAllThrowDivisionByZeroTrRequiresNew() {
        Driver two = new Driver();
        two.setId(2L);
        two.setFio("fio_2");
        two.setDriverLicense("lic_2");
        driverRepository.saveAndFlush(two);
        System.out.println("Division by zero  = " + 1/0);
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
