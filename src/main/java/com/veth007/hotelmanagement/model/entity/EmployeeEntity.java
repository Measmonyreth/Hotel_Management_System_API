package com.veth007.hotelmanagement.model.entity;

import java.util.List;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.veth007.hotelmanagement.constant.enums.Role;
import com.veth007.hotelmanagement.constant.enums.Status;
import com.veth007.hotelmanagement.infrastructure.model.entity.BaseSoftDeleted;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(name = "employees")
@Entity(name = "employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@ToString(exclude = {"shift", "address", "taskEnttities"})
@EqualsAndHashCode(callSuper = false, exclude = {"shift", "address", "taskEnttities"})

public class EmployeeEntity extends BaseSoftDeleted<Long> {

    @Column(name = "employee_id", unique = true, nullable = false)
    private String employeeId;
    
     @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;              // EMPLOYEE, MANAGER, CUSTOMER, ADMIN

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status; 

    @ManyToOne()
    @JoinColumn(name = "shift_id")
    private ShiftEntity shift;

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private AddressEntity address;


    @JsonIgnore
    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY, orphanRemoval = false)
    private List<TaskEnttity> taskEnttities;
}
