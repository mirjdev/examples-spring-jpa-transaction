package com.mirjdev.examplesspring.entity.inheritance_single;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@Entity
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue(value = "doc_bonus")
public class StDocBonus extends StDocParent {

    @Column(name = "cost", precision = 15, scale = 2)
    private BigDecimal cost;

    @Column(name = "phone")
    private String phone;

}