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
@DiscriminatorValue("doc_coupon_4")
public class StDocCoupon4 extends StDocCoupon {

    @Column(name = "coupon_4")
    private String coupon;
}