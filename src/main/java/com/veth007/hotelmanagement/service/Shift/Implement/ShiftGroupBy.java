package com.veth007.hotelmanagement.service.Shift.Implement;

import com.veth007.hotelmanagement.model.request.Shift.UpdateShiftRequest;
import com.veth007.hotelmanagement.model.response.Schedule.ShiftResponse;

import java.time.format.DateTimeFormatter;
import java.util.function.BiConsumer;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShiftGroupBy {
        SHIFT(dto -> dto.getShift() != null ? dto.getShift() : "N/A",
                        (filter, value) -> filter.setShift(value)),
        DAY_WORK(dto -> dto.getDayWork() != null ? dto.getDayWork() : "N/A",
                        (filter, value) -> filter.setDayWork(value)),
        DAY_OFF(dto -> dto.getDayOff() != null ? dto.getDayOff() : "N/A",
                        (filter, value) -> filter.setDayOff(value)),
        STARTING_TIME(dto -> dto.getStartingTime() != null ? dto.getStartingTime() : "N/A",
                        (filter, value) -> filter.setStartingTime(value)),
        ENDING_TIME(dto -> dto.getEndingTime() != null ? dto.getEndingTime() : "N/A",
                        (filter, value) -> filter.setEndingTime(value)),
        CREATED_DATE(dto -> dto.getCreatedDate() != null
                        ? dto.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        : "N/A",
                        (filter, value) -> filter.setCreateDated(value));

        private final Function<ShiftResponse, String> extractor;
        private final BiConsumer<UpdateShiftRequest, String> filterSetter;

        public void applyFilter(UpdateShiftRequest filter, String value) {
                filterSetter.accept(filter, value);
        }

        public String extract(ShiftResponse dto) {
                return extractor.apply(dto);
        }
}
