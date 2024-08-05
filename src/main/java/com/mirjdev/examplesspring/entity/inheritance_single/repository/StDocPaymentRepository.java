package com.mirjdev.examplesspring.entity.inheritance_single.repository;

import com.mirjdev.examplesspring.entity.inheritance_single.StDocPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StDocPaymentRepository extends JpaRepository<StDocPayment, Long> {
}