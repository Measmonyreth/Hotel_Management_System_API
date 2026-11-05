package com.veth007.hotelmanagement.model.request.BookItems;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@Data
@AllArgsConstructor
public class BookItemsRequest implements Serializable {
    @NotNull(message = "Room ID is required")
    private Long roomId;

}
