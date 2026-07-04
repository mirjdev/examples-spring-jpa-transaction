package com.mirjdev.examplesspring.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tasks")
public class Task {

    //    @Id
//    @GenericGenerator(name = "uuid2", strategy = "uuid2")
//    @GeneratedValue(generator = "uuid2")
//    @Type(type = "uuid-char")

    @Id
    @SequenceGenerator(name = "tasks_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tasks_id_seq")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private State state = State.SCHEDULED;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    private TaskType taskType = TaskType.DO_SOMETHING;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "json")
    private String json;

    @Column(name = "attempt", nullable = false)
    private Integer attempt = 0;

    @Column(name = "scheduled_dt", nullable = false)
    private LocalDateTime scheduledDt;

    @Column(name = "complete_dt")
    private LocalDateTime completeDt;

    @Version
    @Column(name = "version")
    private Long version;

    @BatchSize(size = 10)
    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    private List<TaskLog> logs;
}
