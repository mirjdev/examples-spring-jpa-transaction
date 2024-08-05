package com.mirjdev.examplesspring.entity.inheritance_single;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("doc_coupon_5")
public class StDocCoupon5 extends StDocCoupon {

    @Column(name = "coupon_5")
    private String coupon;
}