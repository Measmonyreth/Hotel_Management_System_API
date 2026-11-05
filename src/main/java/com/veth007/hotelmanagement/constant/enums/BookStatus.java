package com.veth007.hotelmanagement.constant.enums;

public enum BookStatus {
    PENDING,     // customer just requested
    CONFIRMED,   // booking approved
    CHECKED_IN,  // customer arrived
    CHECKED_OUT, // customer finished stay
    CANCELLED,   // booking cancelled
    COMPLETED,   // booking completed
    UPCOMING    // booking is upcoming for all book items already hasn't check in 
    // consider CHECKOUT IS COMPLETED
    //
}
