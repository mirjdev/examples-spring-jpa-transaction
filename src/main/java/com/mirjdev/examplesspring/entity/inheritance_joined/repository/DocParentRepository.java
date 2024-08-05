package com.mirjdev.examplesspring.entity.inheritance_joined.repository;

import com.mirjdev.examplesspring.entity.inheritance_joined.DocParent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocParentRepository extends JpaRepository<DocParent, Long> {
}