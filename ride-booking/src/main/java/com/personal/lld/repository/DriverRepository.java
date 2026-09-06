package com.personal.lld.repository;

import com.personal.lld.domain.Driver;
import com.personal.lld.domain.DriverStatus;

import java.util.List;
import java.util.Optional;

public interface DriverRepository {

    Optional<Driver> findById(String id);

    void save(Driver driver);

    List<Driver> findByStatus(DriverStatus status);
}
