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
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@Entity
//@SecondaryTables(@SecondaryTable(name = "doc_bonus", pkJoinColumns = @PrimaryKeyJoinColumn(name = "id")))
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue(value = "doc_bonus")
@PrimaryKeyJoinColumn(name = "id")
@Table(name = "doc_bonus")
public class DocBonus extends DocParent {

    @Column(name = "cost", precision = 15, scale = 2)
    private BigDecimal cost;

    @Column(name = "phone")
    private String phone;
}