package com.sg.obs.base.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;
import java.util.Optional;

@MappedSuperclass
@EntityListeners({AuditingEntityListener.class})
@Getter
@Setter
@JsonIgnoreProperties({"customUpdateBy", "customUpdateDate", "customCreateBy", "customCreateDate"})
public class BaseEntity implements Serializable {


    @Transient
    private String customUpdateBy;

    @Transient
    private Instant customUpdateDate;


    @Transient
    private String customCreateBy;

    @Transient
    private Instant customCreateDate;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    private Instant createdDate;


    @LastModifiedDate
    @Column(name = "UPDATED_DATE")
    private Instant updatedDate;


    @CreatedBy
    @Column(name = "CREATED_BY", length = 50)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "UPDATED_BY", length = 50)
    private String updatedBy;

    @Version
    @Column(name = "VERSION")
    private Long version;

    @PreUpdate
    @PrePersist
    private void onUpdate() {
        audit();
    }

    private void audit() {
        this.updatedBy = Optional.ofNullable(this.customUpdateBy).orElse(this.updatedBy);
        this.updatedDate = Optional.ofNullable(this.customUpdateDate).orElse(this.updatedDate);

        this.createdBy = Optional.ofNullable(this.customCreateBy).orElse(this.createdBy);
        this.createdDate = Optional.ofNullable(this.customCreateDate).orElse(this.createdDate);
    }
}


