package com.personal.lld.repository;

import com.personal.lld.domain.Location;

public interface LocationRepository {

    void saveLocation(String driverId, Location location);

    Location getLatestLocation(String driverId);
}
