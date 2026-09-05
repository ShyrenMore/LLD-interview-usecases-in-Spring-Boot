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
public class RoomTypeAvailability {
    private String roomTypeId;
    private String roomTypeName;
    private int capacity;
    private String bedType;
    private List<String> amenities;
    private boolean available;
    private long totalPriceMinor;
    private double averagePricePerNight;
    private List<NightlyPrice> nightlyPrices;
}
