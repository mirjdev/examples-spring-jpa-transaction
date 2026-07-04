package com.mirjdev.examplesspring.entity.repository;

import com.mirjdev.examplesspring.aop.TransactionMonitoring;
import com.mirjdev.examplesspring.entity.Driver;
import com.mirjdev.examplesspring.entity.Task;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    //для примера
    String SKIP_LOCKED = "-2";

    @Lock(LockModeType.PESSIMISTIC_READ)
    @QueryHints(@QueryHint(name = AvailableSettings.JPA_LOCK_TIMEOUT, value = SKIP_LOCKED))
    @Query(value = "select t from Task t where t.id=:id")
    Optional<Driver> tryFindById(Long id);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @NativeQuery("select * from tasks  where complete_dt is null and scheduled_dt <= current_timestamp " +
            " and state='SCHEDULED' " +
            " order by scheduled_dt " +
            " limit :limit for update skip locked")
    List<Task> findTasks(int limit);

    @Transactional
    @Modifying
    @NativeQuery("update tasks set state='SCHEDULED' where state='IN_PROGRESS'")
    void reScheduleTasks();

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    default Task findTaskByIdWithOptLock(Long id) {
        return findById(id).orElseThrow();
    }

    @TransactionMonitoring
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    default Task findTaskByIdWithoutOptLock(Long id) {
        return findById(id).orElseThrow();
    }


}