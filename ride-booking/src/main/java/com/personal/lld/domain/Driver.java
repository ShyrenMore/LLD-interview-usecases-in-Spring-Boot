package com.personal.lld.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Driver {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String vehicleNumber;
    private String vehicleType;
    private DriverStatus status;
    private Location currentLocation;
    private long lastLocationUpdate;
}
