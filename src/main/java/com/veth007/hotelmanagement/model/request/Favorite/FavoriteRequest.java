package com.veth007.hotelmanagement.model.request.Favorite;

import java.io.Serializable;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteRequest implements Serializable {
    // @NotNull(message = "Customer id is required")
    // private Long customerId;

    @NotNull(message = "Room id is required")
    private Long roomId;
}
