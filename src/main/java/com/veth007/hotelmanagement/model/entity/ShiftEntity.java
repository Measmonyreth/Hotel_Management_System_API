package com.veth007.hotelmanagement.model.entity;

import java.time.LocalTime;
import java.util.List;

import com.veth007.hotelmanagement.constant.enums.Shift;
import com.veth007.hotelmanagement.infrastructure.model.entity.BaseSoftDeleted;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftEntity extends BaseSoftDeleted<Long>{
       @Column(name = "day_work")
    private String dayWork;        // e.g. "Monday, Tuesday, Wednesday"

    @Column(name = "day_off")
    private String dayOff;         // e.g. "Saturday, Sunday"

    @Column(name = "starting_time")
    private LocalTime  startingTime; // e.g. 08:00

    @Column(name = "ending_time")
    private LocalTime  endingTime;   // e.g. 17:00

    @Enumerated(EnumType.STRING)
    private Shift shift;   
    
    @OneToMany(mappedBy = "shift")
    private List<EmployeeEntity> employees;
}
