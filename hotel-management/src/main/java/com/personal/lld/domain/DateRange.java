package com.personal.lld.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DateRange {
    private long checkInDateUtc;
    private long checkOutDateUtc; // exclusive

    public int numberOfNights(long millisPerDay) {
        if (checkOutDateUtc <= checkInDateUtc) {
            return 0;
        }
        return Math.toIntExact((checkOutDateUtc - checkInDateUtc) / millisPerDay);
    }
}
