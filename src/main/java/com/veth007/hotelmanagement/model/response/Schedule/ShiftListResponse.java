package com.veth007.hotelmanagement.model.response.Schedule;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ShiftListResponse {
    private int totalShifts;
    private int totalWorkFromMondayToFriday;
    private int totalWorkFromMondayToSaturday;
    private int totalWorkFromSundayToFriday;
    private int totalTimeFrom8AMTo5PM;
    private int totalTimeFrom9AMTo6PM;
    private int totalFullTime;
    private int totalPartTime;
    private List<ShiftResponse> shifts;
}
