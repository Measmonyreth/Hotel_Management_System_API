package com.veth007.hotelmanagement.model.request.Payment;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest implements Serializable {
    // for payment next of id book
    private Long bookId;
    @NotBlank(message = "Amount is required")
    private BigDecimal paidAmount;

    @NotBlank(message = "Currency is required")
    private String currency;

    private String email;
    private String description;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
    @NotBlank(message = "Payment timing is required")
    private String paymentTiming;
    
    private LocalDateTime paymentDate;
    private String note;

}
