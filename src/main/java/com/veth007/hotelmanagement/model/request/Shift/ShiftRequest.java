package com.veth007.hotelmanagement.model.request.Shift;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftRequest implements Serializable{
    @NotBlank(message = "Day work is required")
    private String dayWork;
    @NotBlank(message = "Day off is required")
    private String dayOff;
    @NotBlank(message = "Starting time is required")
    private String startingTime;
    @NotBlank(message = "Ending time is required")
    private String endingTime;
    @NotBlank(message = "Shift is required")
    private String shift;

}
