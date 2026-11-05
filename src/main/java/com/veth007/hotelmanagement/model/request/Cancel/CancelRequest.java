package com.veth007.hotelmanagement.model.request.Cancel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancelRequest implements java.io.Serializable {
    private Long bookId;
    private String reason; // Why the guest is cancelling
    private String status;
    private String timing;
    private String rejectionReason;
    private Double calculatedPenaltyFee;
    private Double calculatedRefundAmount;
}
