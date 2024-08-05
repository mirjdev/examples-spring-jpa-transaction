package com.mirjdev.examplesspring.entity.inheritance_joined.repository;

import com.mirjdev.examplesspring.entity.inheritance_joined.DocCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocCouponRepository extends JpaRepository<DocCoupon, Long> {
}