package com.veth007.hotelmanagement.model.request.Discount;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateDiscountRequest {
    private String code;
    private String description;
    private String type;
    private Double percentage;
    private String status;
    private String valid_from;
    private String valid_until;
    private Long vipId;
    private Long bookId;
}
