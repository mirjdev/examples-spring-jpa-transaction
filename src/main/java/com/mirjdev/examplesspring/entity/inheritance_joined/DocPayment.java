package com.mirjdev.examplesspring.entity.inheritance_joined;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@ToString
@EqualsAndHashCode(callSuper = true)
@Table(name = "doc_payment")
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("doc_payment")
public class DocPayment extends DocParent {

    @Column(name = "cost", precision = 15, scale = 2)
    private BigDecimal cost;

    @Column(name = "source_id")
    private UUID sourceId;

    @Column(name = "user_id")
    private UUID userId;
}