package com.mirjdev.examplesspring.service;

import com.mirjdev.examplesspring.entity.Driver;
import com.mirjdev.examplesspring.entity.repository.DriverRepository;
import com.mirjdev.examplesspring.postgres.PostgresTestcontainerInitializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@ActiveProfiles("test-tc-and-h2")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = PostgresTestcontainerInitializer.class)
@SpringBootTest
@ExtendWith(SpringExtension.class)
class IsolationLevelFacadeTest {

    @Autowired
    private IsolationLevelFacade isolationLevelFacade;
    @Autowired
    private IsolationLevelService isolationLevelService;

    @Autowired
    private DriverRepository driverRepository;

    private static final String FIO_DEFAULT = "fio_ver1";

    @BeforeEach
    void setUp() {
        driverRepository.deleteAll();
        driverRepository.saveAndFlush(
                Driver.builder()
                        .id(1L)
                        .fio(FIO_DEFAULT)
                        .driverLicense("A1")
                        .build()
        );
    }

    @Test
    void testThrowInner() {
        try {
            isolationLevelFacade.testThrowInner();
        } catch (Exception e) {

        }
        List<Driver> drivers = driverRepository.findAll();
        Assertions.assertEquals(1, drivers.size());
        Assertions.assertEquals(FIO_DEFAULT, drivers.get(0).getFio());
        System.out.println("In test driverRepository.findAll() = " + drivers);
    }

    @Test
    void testThrowOuter1() {
        try {
            isolationLevelFacade.testThrowOuter1();
        } catch (Exception e) {
        }
        String fio = driverRepository.getOne(1L).getFio();
        // основная транзакция ОТКАТИЛА свои изменения
        Assertions.assertEquals(FIO_DEFAULT, fio);
    }

    @Test
    void testThrowOuter2() {
        try {
            isolationLevelFacade.testThrowOuter2();
        } catch (Exception e) {
        }
        int size = driverRepository.findAll().size();
        String fio = driverRepository.getOne(1L).getFio();
        // основная транзакция СОХРАНИЛА свои изменения
        Assertions.assertEquals("Update fio.ver 2", fio);
        Assertions.assertEquals(1, size);
    }
}