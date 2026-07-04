package com.mirjdev.examplesspring.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tasks_logs")
public class TaskLog {

    @Id
    @SequenceGenerator(name = "tasks_logs_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tasks_logs_id_seq")
    @Column(name = "logs_id")
    private Long id;

    @Column(name = "log")
    private String log;

    @JoinColumn(name = "task_id")
    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private Task task;

}
