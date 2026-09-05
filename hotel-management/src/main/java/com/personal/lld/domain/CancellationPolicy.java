package com.personal.lld.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancellationPolicy {
    private String id;
    private String name;
    private int refundPercent;
    private int cutoffHoursBeforeCheckIn;
    private long createdAt;
}
