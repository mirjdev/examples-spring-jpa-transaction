package com.mirjdev.examplesspring.entity.inheritance_single.repository;

import com.mirjdev.examplesspring.entity.inheritance_single.StDocCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StDocCouponRepository extends JpaRepository<StDocCoupon, Long> {
}