package com.mirjdev.examplesspring.entity.inheritance_joined;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Getter
@Setter
@ToString
@Entity
@Table(name = "doc_parent")
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@Inheritance(strategy = InheritanceType.JOINED)
public class DocParent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "doc_parent_id_gen")
    @SequenceGenerator(name = "doc_parent_id_gen", sequenceName = "doc_parent_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "type", insertable = false, updatable = false, nullable = false)
    private String type;

    @NotNull
    @Column(name = "create_dt", insertable = false, updatable = false, nullable = false)
    private Instant createDt;

}