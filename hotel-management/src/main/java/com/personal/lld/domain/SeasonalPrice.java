package com.personal.lld.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeasonalPrice {
    private String id;
    private String hotelId;
    private String roomTypeId;
    private long dateUtc;
    private long priceMinor;
    private long createdAt;
}
