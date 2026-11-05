package com.veth007.hotelmanagement.service.BookingWithPayment.Booking_Payment.Implement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public final class BookingBalanceCalculator {

    private BookingBalanceCalculator() {
    }

public static List<BigDecimal> calculateItemRemainingAmounts(
        BigDecimal totalAmount,
        BigDecimal depositAmount,
        List<BigDecimal> itemTotals) {

    if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
        return List.of();
    }

    BigDecimal effectiveDeposit = depositAmount != null ? depositAmount : BigDecimal.ZERO;
    BigDecimal paidAmount = effectiveDeposit.min(totalAmount).max(BigDecimal.ZERO);
    BigDecimal bookRemaining = totalAmount.subtract(paidAmount).max(BigDecimal.ZERO);

    // only items with a positive price participate in the split
    long validCount = itemTotals.stream()
            .filter(t -> t != null && t.compareTo(BigDecimal.ZERO) > 0)
            .count();

    List<BigDecimal> remainingAmounts = new ArrayList<>();

    if (validCount == 0 || bookRemaining.compareTo(BigDecimal.ZERO) <= 0) {
        itemTotals.forEach(t -> remainingAmounts.add(BigDecimal.ZERO));
        return remainingAmounts;
    }

    BigDecimal equalShare = bookRemaining.divide(
            BigDecimal.valueOf(validCount), 2, RoundingMode.HALF_UP);

    BigDecimal allocatedSoFar = BigDecimal.ZERO;
    long seen = 0;

    for (BigDecimal itemTotal : itemTotals) {
        if (itemTotal == null || itemTotal.compareTo(BigDecimal.ZERO) <= 0) {
            remainingAmounts.add(BigDecimal.ZERO);
            continue;
        }

        seen++;
        BigDecimal itemTotalScaled = itemTotal.setScale(2, RoundingMode.HALF_UP);

        // last valid item absorbs the rounding remainder so totals reconcile exactly
        BigDecimal share = (seen == validCount)
                ? bookRemaining.subtract(allocatedSoFar)
                : equalShare;

        // an item can never owe more than its own price
        BigDecimal itemRemaining = share.min(itemTotalScaled).max(BigDecimal.ZERO);
        allocatedSoFar = allocatedSoFar.add(itemRemaining);
        remainingAmounts.add(itemRemaining);
    }

    return remainingAmounts;
}
}
