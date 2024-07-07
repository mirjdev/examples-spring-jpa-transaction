package com.mirjdev.examplesspring.entity.repository;

import com.mirjdev.examplesspring.entity.Driver;
import com.mirjdev.examplesspring.entity.Task;
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
    @Query(value = "select * from tasks  where complete_dt is null and scheduled_dt <= current_timestamp " +
            " and state not in ('ERROR', 'CANCEL', 'COMPLETE') " +
            " order by scheduled_dt " +
            " limit :limit for update skip locked", nativeQuery = true)
    List<Task> findTasks(int limit);

    @Transactional
    @Modifying
    @Query(value = "update tasks set state='SCHEDULED' where state='IN_PROGRESS'", nativeQuery = true)
    void reScheduleTasks();

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    default Task findTaskByIdWithOptLock(Long id) {
        return findById(id).orElseThrow();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    default Task findTaskByIdWithoutOptLock(Long id) {
        return findById(id).orElseThrow();
    }


}