package com.veth007.hotelmanagement.infrastructure.model.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseSoftDeleted<ID extends Serializable> extends BaseAuditing<Long> {
    
     @Column(name = "deleted_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime deletedDate;

    @Column(name = "delete_by")
    private String deletedBy;

    public String getDeleted_by() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public LocalDateTime getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(LocalDateTime deletedDate) {
        this.deletedDate = deletedDate;
    }
}
