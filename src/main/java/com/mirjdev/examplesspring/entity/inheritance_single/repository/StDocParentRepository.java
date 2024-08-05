package com.mirjdev.examplesspring.entity.inheritance_single.repository;

import com.mirjdev.examplesspring.entity.inheritance_single.StDocParent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StDocParentRepository extends JpaRepository<StDocParent, Long> {
}