package com.personal.lld.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SearchFilter {
    private String city;
    private String country;
    private Long checkInDateUtc;
    private Long checkOutDateUtc;
    private String bedType;
    private Long minPriceMinor;
    private Long maxPriceMinor;
}
