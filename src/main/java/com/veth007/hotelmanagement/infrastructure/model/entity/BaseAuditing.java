package com.veth007.hotelmanagement.infrastructure.model.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseAuditing<ID extends Serializable> extends BaseEntity<Long> {
    
    @Column(name = "created_date", columnDefinition = "TIMESTAMP")
    @CreatedDate
    private LocalDateTime createdDate;

    @Column(name = "last_modified_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime lastModifiedDate;

    @Column(name = "create_by", updatable = false)
    @CreatedBy
    private String createdBy;

      @Column(name = "last_modified_by")
    private String lastModifiedBy;

      public LocalDateTime getCreatedDate() {
          return createdDate;
      }

      public void setCreatedDate(LocalDateTime createdDate) {
          this.createdDate = createdDate;
      }

      public LocalDateTime getLastModifiedDate() {
          return lastModifiedDate;
      }

      public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
          this.lastModifiedDate = lastModifiedDate;
      }

      public String getCreatedBy() {
          return createdBy;
      }

      public void setCreatedBy(String createdBy) {
          this.createdBy = createdBy;
      }

      public String getLastModifiedBy() {
          return lastModifiedBy;
      }

      public void setLastModifiedBy(String lastModifiedBy) {
          this.lastModifiedBy = lastModifiedBy;
      }

}
