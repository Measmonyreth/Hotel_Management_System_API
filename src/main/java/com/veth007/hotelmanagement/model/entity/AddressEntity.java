package com.veth007.hotelmanagement.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.veth007.hotelmanagement.infrastructure.model.entity.BaseSoftDeleted;

@Table(name = "address")
@Entity(name = "address")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@ToString(exclude = "employee")
@EqualsAndHashCode(callSuper = false, exclude = "employee")
public class AddressEntity extends BaseSoftDeleted<Long> {
    @Column(name = "vilage")
    private String village;

    @Column(name  = "commun")
    private String commun;

    @Column(name = "city_or_province", nullable = false)
    private String city_or_province;

    @Column(name = "country", nullable = false)
    private String country;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employee;
}
