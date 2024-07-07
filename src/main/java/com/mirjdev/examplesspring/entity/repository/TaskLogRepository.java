package com.mirjdev.examplesspring.entity.repository;

import com.mirjdev.examplesspring.entity.TaskLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskLogRepository extends JpaRepository<TaskLog, Long> {
}