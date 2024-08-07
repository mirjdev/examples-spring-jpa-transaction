package com.mirjdev.examplesspring.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;
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
