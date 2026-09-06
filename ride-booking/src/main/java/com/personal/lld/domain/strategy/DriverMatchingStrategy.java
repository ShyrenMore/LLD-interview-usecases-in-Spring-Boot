package com.personal.lld.domain.strategy;

import com.personal.lld.domain.Driver;
import com.personal.lld.domain.Location;

import java.util.List;

public interface DriverMatchingStrategy {

    List<Driver> findMatchingDrivers(
            Location pickup,
            List<Driver> candidates,
            int maxResults);
}
