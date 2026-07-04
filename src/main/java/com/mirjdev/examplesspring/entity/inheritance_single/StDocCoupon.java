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
//@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("doc_coupon")
public class StDocCoupon extends StDocBonus {

    @Column(name = "coupon")
    private String coupon;
}