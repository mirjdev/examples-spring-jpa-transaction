package com.mirjdev.examplesspring.entity.repository;

import com.mirjdev.examplesspring.entity.Driver;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    @Transactional(propagation = Propagation.REQUIRED)
    @Modifying
    @Query(value = "update drivers set fio=:fio where id > 0;", nativeQuery = true)
    void updateAllDrFioNativeRequired(String fio);

    @Modifying
    @Query(value = "update drivers set fio=:fio where id > 0;", nativeQuery = true)
    void updateAllDrFioNative(String fio);

    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<Driver> findById(Long id);

    @Query("select d from Driver d where d.id=:id")
    Optional<Driver> findByIdNoLock(Long id);


    String SKIP_LOCKED = "-2";

    @Lock(LockModeType.PESSIMISTIC_READ)
    @QueryHints(@QueryHint(name = AvailableSettings.JPA_LOCK_TIMEOUT, value = SKIP_LOCKED))
    @Query(value = "select d from Driver d where d.id=:id")
    Optional<Driver> tryFindById(Long id);
}