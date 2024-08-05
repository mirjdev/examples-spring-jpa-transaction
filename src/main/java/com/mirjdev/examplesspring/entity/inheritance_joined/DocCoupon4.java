package com.mirjdev.examplesspring.entity.inheritance_joined;

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
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("doc_coupon_4")
@Table(name = "doc_coupon_4")
public class DocCoupon4 extends DocCoupon {

    @Column(name = "coupon_4")
    private String coupon;
}