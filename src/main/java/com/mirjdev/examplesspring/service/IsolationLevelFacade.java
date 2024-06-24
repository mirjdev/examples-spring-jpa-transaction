package com.mirjdev.examplesspring.service;


import com.mirjdev.examplesspring.entity.Driver;
import com.mirjdev.examplesspring.entity.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IsolationLevelFacade {

    private final IsolationLevelService isolationLevelService;
    private final DriverRepository driverRepository;

    @SneakyThrows
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public String testRR() {
        // получаем водителя, в RR
        Driver driver = isolationLevelService.findFirstDriverRR();
        System.out.println("!!!!!!!!! STEP 1 isolationLevelService.findFirstDriverRR  = " + driver);

        Thread.sleep(3000L);
        driver.setFio("fio_ver3");
        Driver saved = isolationLevelService.saveRRAndMandatory(driver);
        System.out.println("!!!!!!!!! STEP 4 saveRRAndMandatory = " + saved);
        return driver.toString();
    }

    public void updateFirstDriver() {
        // Получаем без RR
        Driver firstDriver = isolationLevelService.findFirstDriver();
        // Обновляем в другой транзакции
        firstDriver.setFio("fio_ver2");
        firstDriver.setComment("abra-cadabra");
        System.out.println("!!!!!!!!! STEP 2 firstDriver = " + firstDriver);
        Driver updatedDriver = isolationLevelService.updateDriver(firstDriver);
        System.out.println("!!!!!!!!! STEP 3 isolationLevelService.updateDriver = " + updatedDriver);

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateAllDr() {
        driverRepository.updateAllDr();
    }
}
