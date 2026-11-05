package com.veth007.hotelmanagement.model.request.Shift;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShiftRequest implements Serializable{
        private String dayWork;
    private String dayOff;
    private String startingTime;
    private String endingTime;
    private String shift;
    private String createDated;
}
