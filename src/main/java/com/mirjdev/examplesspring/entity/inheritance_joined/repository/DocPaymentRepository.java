package com.mirjdev.examplesspring.entity.inheritance_joined.repository;

import com.mirjdev.examplesspring.entity.inheritance_joined.DocPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocPaymentRepository extends JpaRepository<DocPayment, Long> {
}