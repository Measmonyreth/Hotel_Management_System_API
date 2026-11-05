package com.veth007.hotelmanagement.model.response.Schedule;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.veth007.hotelmanagement.config.HashIdUtil;
import com.veth007.hotelmanagement.model.entity.ShiftEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftResponse implements Serializable {
    private Long id;
    private String dayWork;
    private String dayOff;
    private String startingTime;
    private String endingTime;
    private String shift;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private LocalDateTime deletedDate;
    private String createBy;
    private String lastModifiedBy;
    private String deletedBy;

    public static ShiftResponse fromEntity(ShiftEntity shift) {
        return ShiftResponse.builder()
                .id(shift.getId())
                .dayWork(shift.getDayWork())
                .dayOff(shift.getDayOff())
                .startingTime(shift.getStartingTime().toString())
                .endingTime(shift.getEndingTime().toString())
                .shift(shift.getShift().name())
                .createdDate(shift.getCreatedDate())
                .lastModifiedDate(shift.getLastModifiedDate())
                .deletedDate(shift.getDeletedDate())
                .createBy(shift.getCreatedBy())
                .lastModifiedBy(shift.getLastModifiedBy())
                .deletedBy(shift.getDeleted_by())
                .build();
    }
}
