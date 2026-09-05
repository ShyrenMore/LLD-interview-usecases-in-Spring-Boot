package com.personal.lld.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    private String id;
    private String userId;
    private String hotelId;
    private String roomTypeId;
    private long checkInDateUtc;
    private long checkOutDateUtc; // exclusive
    private List<NightlyPrice> nightlyPrices;
    private long totalAmountMinor;
    private BookingStatus bookingStatus;
    private TransactionStatus paymentStatus;
    private String allocatedRoomId;
    private long checkInTimeUtc;
    private long checkOutTimeUtc;
    private long holdExpiresAt;
    private long createdAt;

    @Override
    public String toString() {
        return "Booking{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", hotelId='" + hotelId + '\'' +
                ", roomTypeId='" + roomTypeId + '\'' +
                ", bookingStatus=" + bookingStatus +
                ", paymentStatus=" + paymentStatus +
                ", totalAmountMinor=" + totalAmountMinor +
                '}';
    }
}
