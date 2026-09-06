package com.personal.lld.repository;

import com.personal.lld.domain.Ride;
import com.personal.lld.domain.RideStatus;

import java.util.List;
import java.util.Optional;

public interface RideRepository {

    Optional<Ride> findById(String id);

    void save(Ride ride);

    List<Ride> findByRiderId(String riderId);

    List<Ride> findByDriverId(String driverId);

    List<Ride> findByStatus(RideStatus status);
}
