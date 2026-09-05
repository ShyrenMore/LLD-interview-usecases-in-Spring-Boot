package com.personal.lld.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DateRange {

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public DateRange(
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
