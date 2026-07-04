package com.mirjdev.examplesspring.entity.inheritance_single;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Setter
@ToString
@Entity
@Table(name = "st_doc_parent")
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class StDocParent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "st_doc_parent_id_gen")
    @SequenceGenerator(name = "st_doc_parent_id_gen", sequenceName = "st_doc_parent_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "type", insertable = false, updatable = false, nullable = false)
    private String type;

    @NotNull
    @Column(name = "create_dt", insertable = false, updatable = false, nullable = false)
    private Instant createDt;

}