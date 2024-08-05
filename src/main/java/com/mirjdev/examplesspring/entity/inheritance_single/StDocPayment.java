package com.mirjdev.examplesspring.entity.inheritance_single;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@ToString
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("doc_payment")
public class StDocPayment extends StDocParent {

    @Column(name = "cost", precision = 15, scale = 2)
    private BigDecimal cost;

    @Column(name = "source_id")
    private UUID sourceId;

    @Column(name = "user_id")
    private UUID userId;
}