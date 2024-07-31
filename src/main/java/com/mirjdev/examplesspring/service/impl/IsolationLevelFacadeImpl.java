package com.mirjdev.examplesspring.service.impl;


import com.mirjdev.examplesspring.entity.Driver;
import com.mirjdev.examplesspring.entity.repository.DriverRepository;
import com.mirjdev.examplesspring.service.IsolationLevelFacade;
import com.mirjdev.examplesspring.service.IsolationLevelService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IsolationLevelFacadeImpl implements IsolationLevelFacade {

    private final IsolationLevelService isolationLevelService;
    private final DriverRepository driverRepository;

    public static final String FIO_VER_2 = "Update fio.ver 2";
    public static final String FIO_VER_3 = "Update fio.ver 3";

    @Override
    @SneakyThrows
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public String testRR() {
        // получаем водителя, в RR
        Driver driver = isolationLevelService.findFirstDriverRR();
        System.out.println("!!!!!!!!! STEP 1 isolationLevelService.findFirstDriverRR  = " + driver);

        Thread.sleep(3000L);
        driver.setFio(FIO_VER_3);
        Driver saved = isolationLevelService.saveRRAndMandatory(driver);
        System.out.println("!!!!!!!!! STEP 4 saveRRAndMandatory = " + saved);
        return driver.toString();
    }

    @Override
    public void updateFirstDriver() {
        // Получаем без RR
        Driver firstDriver = isolationLevelService.findFirstDriver();
        // Обновляем в другой транзакции
        firstDriver.setFio(FIO_VER_2);
        firstDriver.setComment("abra-cadabra");
        System.out.println("!!!!!!!!! STEP 2 firstDriver = " + firstDriver);
        Driver updatedDriver = isolationLevelService.updateDriver(firstDriver);
        System.out.println("!!!!!!!!! STEP 3 isolationLevelService.updateDriver = " + updatedDriver);

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateAllDr() {
        driverRepository.updateAllDrFioNativeRequired("");
    }

    /**
     * Откат, если вложенный(REQUIRED) метод упал на стороне базы данных (org.springframework.dao.DataIntegrityViolationException)
     * Откатится всё, дальнейшие запросы невозможны
     */
    @Override
    @Transactional
    public void testThrowInner() {
        Driver driver = isolationLevelService.findFirstDriver();
        driver.setFio(FIO_VER_2);
        driverRepository.saveAndFlush(driver);
        System.out.println("testThrowInner1 driverRepository.findAll() = " + driverRepository.findAll());

        try {
            // Если возник эксепшин на стороне базы, допустим валидация, транзакция в любом случае не сможет быть продолжена
            // пруф 95 страница книги Postgres 16
            // ERROR: current transaction is aborted, commands ignored until end of transaction block
            driverRepository.updateAllDrFioNativeRequired("a".repeat(256));
        } catch (Exception ex) {
            System.out.println("exception2 = " + ex);
        }
        // .e.j.s.SqlExceptionHelper|[ERROR]|main                |
        // ERROR: current transaction is aborted, commands ignored until end of transaction block
        List<Driver> drivers = driverRepository.findAll();
    }

    @Override
    @Transactional(noRollbackFor = ArithmeticException.class)
    public void testThrowInner2() {
        Driver driver = isolationLevelService.findFirstDriver();
        driver.setFio(FIO_VER_2);
        driverRepository.saveAndFlush(driver);
        System.out.println("testThrowInner1 driverRepository.findAll() = " + driverRepository.findAll());
        System.out.println("driver = " + 1 / 0);
    }

    /**
     * Откат, если вложенный(REQUIRED) метод упал с RuntimeException
     * Важный момент, границы методов должны быть пересечены (один TR -> внутри другой TR)
     */
    @Override
    @Transactional
    public void testThrowOuter1() {
        Driver driver = isolationLevelService.findFirstDriver();
        driver.setFio(FIO_VER_2);
        driverRepository.saveAndFlush(driver);
        System.out.println("testThrowOuter driverRepository.findAll() = " + driverRepository.findAll());
        try {
            isolationLevelService.updateAllThrowDivisionByZeroTrRequired();
        } catch (Exception exception) {
            System.out.println("exception = " + exception);
        }
    }

    /**
     * Откат внешней(REQUIRES_NEW) транзакции не приводить к откату основной, если обработать
     * 1. Созданному водителю меняем фио
     * 2. В новой транзакции добавляем еще одного, и падаем RuntimeException
     * 3. Вставка откатывается, но так мы обработали падение из новой транзакции, изменения в первой будут сохранены
     */
    @Override
    @Transactional
    public void testThrowOuter2() {
        Driver driver = isolationLevelService.findFirstDriver();

        driver.setFio(FIO_VER_2);
        driverRepository.saveAndFlush(driver);
        System.out.println("testThrowOuter driverRepository.findAll() = " + driverRepository.findAll());
        try {
            isolationLevelService.updateAllThrowDivisionByZeroTrRequiresNew();
        } catch (Exception exception) {
            System.out.println("exception = " + exception);
        }
    }

    /**
     * Отката не будет так как Checked Exception
     * 1. Созданному водителю меняем фио
     * 2. В новой транзакции добавляем еще одного, и падаем IOException
     * 3. Так Checked Exception, основная транзакция будет завершена, внутренняя тоже сохранит данные
     * 4. Ни одна транзакция не будет
     */
    @Override
    @Transactional
    public void testThrowOuter3() {
        Driver driver = isolationLevelService.findFirstDriver();
        driver.setFio(FIO_VER_2);
        driverRepository.saveAndFlush(driver);
        System.out.println("testThrowOuter driverRepository.findAll() = " + driverRepository.findAll());
        try {
            isolationLevelService.updateAllThrowCheckedException();
        } catch (Exception exception) {
            System.out.println("exception = " + exception);
        }
    }
}
