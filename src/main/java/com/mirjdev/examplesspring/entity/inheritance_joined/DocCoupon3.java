package com.mirjdev.examplesspring.entity.inheritance_joined;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@Entity
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("doc_coupon_3")
@Table(name = "doc_coupon_3")
public class DocCoupon3 extends DocCoupon {

    @Column(name = "coupon_3")
    private String coupon;
}