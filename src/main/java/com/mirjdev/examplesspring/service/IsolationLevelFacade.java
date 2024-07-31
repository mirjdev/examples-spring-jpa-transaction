package com.mirjdev.examplesspring.service;

import lombok.SneakyThrows;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface IsolationLevelFacade {
    @SneakyThrows
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    String testRR();

    void updateFirstDriver();

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void updateAllDr();

    @Transactional
    void testThrowInner();

    @Transactional(noRollbackFor = ArithmeticException.class)
    void testThrowInner2();

    @Transactional
    void testThrowOuter1();

    @Transactional
    void testThrowOuter2();

    @Transactional
    void testThrowOuter3();
}
