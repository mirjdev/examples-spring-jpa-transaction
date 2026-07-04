package com.mirjdev.examplesspring.entity.inheritance_joined;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@Entity
//@SecondaryTables(@SecondaryTable(name = "doc_bonus", pkJoinColumns = @PrimaryKeyJoinColumn(name = "id")))
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue(value = "doc_bonus")
@Inheritance(strategy = InheritanceType.JOINED)
@PrimaryKeyJoinColumn(name = "id")
@Table(name = "doc_bonus")
public class DocBonus extends DocParent {

    @Column(name = "cost", precision = 15, scale = 2)
    private BigDecimal cost;

    @Column(name = "phone")
    private String phone;
}