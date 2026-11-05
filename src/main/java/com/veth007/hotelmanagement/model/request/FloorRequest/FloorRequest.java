package com.veth007.hotelmanagement.model.request.FloorRequest;

import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FloorRequest implements Serializable {
    
    @NotBlank(message = "Floor name is required")
    private String floorName;
    @NotNull(message = "Floor number is required")
    private String floorNumber;

    private String description;

}
