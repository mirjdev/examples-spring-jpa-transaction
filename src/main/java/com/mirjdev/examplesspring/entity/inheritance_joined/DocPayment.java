package com.mirjdev.examplesspring.entity.inheritance_joined;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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