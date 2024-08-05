package com.mirjdev.examplesspring.entity.inheritance_single.repository;

import com.mirjdev.examplesspring.entity.inheritance_single.StDocBonus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StDocBonusRepository extends JpaRepository<StDocBonus, Long> {
}