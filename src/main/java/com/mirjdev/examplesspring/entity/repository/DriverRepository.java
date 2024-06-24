package com.mirjdev.examplesspring.entity.repository;

import com.mirjdev.examplesspring.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    @Modifying
    @Query(value = "update drivers set fio='all_update' where id > 0;", nativeQuery = true)
    void updateAllDr();

}