package com.mirjdev.examplesspring.entity.repository;

import com.mirjdev.examplesspring.entity.Driver;
import com.mirjdev.examplesspring.entity.Task;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

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

    @Query(value = "select * from tasks  where complete_dt is null and scheduled_dt <= current_timestamp and state not in ('ERROR', 'CANCEL') limit :limit for update skip locked", nativeQuery = true)
    List<Task> findTasks(int limit);

}