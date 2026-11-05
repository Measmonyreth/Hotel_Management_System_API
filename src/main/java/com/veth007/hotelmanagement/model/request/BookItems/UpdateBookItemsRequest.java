package com.veth007.hotelmanagement.model.request.BookItems;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@Data
@AllArgsConstructor
public class UpdateBookItemsRequest implements Serializable{
        private String bookingType;
    private String status;
    private String checkInDate;
    private String checkOutDate;
}
