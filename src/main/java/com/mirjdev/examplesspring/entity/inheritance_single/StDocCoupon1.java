package com.mirjdev.examplesspring.entity.inheritance_single;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("doc_coupon_1")
public class StDocCoupon1 extends StDocCoupon {

    @Column(name = "coupon_1")
    private String coupon;
}