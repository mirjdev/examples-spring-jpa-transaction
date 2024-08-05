package com.mirjdev.examplesspring.entity.inheritance_joined.repository;

import com.mirjdev.examplesspring.entity.inheritance_joined.DocBonus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocBonusRepository extends JpaRepository<DocBonus, Long> {
}