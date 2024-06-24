package com.mirjdev.examplesspring.entity;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners({AuditingEntityListener.class})
public abstract class EntityWithTimestamps implements Serializable {

    @Version
    @Column(
            name = "version"
    )
    private Long version;
    @CreatedDate
    @Column(
            name = "date_ins"
    )
    private LocalDateTime dateIns;
    @LastModifiedDate
    @Column(
            name = "date_modif"
    )
    private LocalDateTime dateModif;
    @CreatedBy
    @Column(
            name = "user_ins"
    )
    private String userIns;
    @LastModifiedBy
    @Column(
            name = "user_modif"
    )
    private String userModif;

    public EntityWithTimestamps() {
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public LocalDateTime getDateIns() {
        return dateIns;
    }

    public void setDateIns(LocalDateTime dateIns) {
        this.dateIns = dateIns;
    }

    public LocalDateTime getDateModif() {
        return dateModif;
    }

    public void setDateModif(LocalDateTime dateModif) {
        this.dateModif = dateModif;
    }

    public String getUserIns() {
        return userIns;
    }

    public void setUserIns(String userIns) {
        this.userIns = userIns;
    }

    public String getUserModif() {
        return userModif;
    }

    public void setUserModif(String userModif) {
        this.userModif = userModif;
    }

}

