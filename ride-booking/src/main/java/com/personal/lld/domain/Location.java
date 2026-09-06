package com.personal.lld.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private double latitude;
    private double longitude;
    private String address;
    private long timestamp;
}
