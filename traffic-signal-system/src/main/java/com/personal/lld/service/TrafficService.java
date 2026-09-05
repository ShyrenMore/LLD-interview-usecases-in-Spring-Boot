package com.personal.lld.service;

import com.personal.lld.domain.VehicleCounter;
import com.personal.lld.domain.enums.Direction;
import com.personal.lld.repository.TrafficRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrafficService {

    private final TrafficRepository trafficRepository;

    @PostConstruct
    public void init() {
        log.info("TrafficService initialized");
    }

    public void updateVehicleCount(Direction direction, int count) {
        trafficRepository.updateCount(direction, count);
        log.info("Vehicle count updated for {}: {}", direction, count);

        // In a real implementation, this could trigger dynamic timing adjustments
        log.debug("TODO: Trigger dynamic timing adjustment if enabled");
    }

    public int getVehicleCount(Direction direction) {
        int count = trafficRepository.getCount(direction);
        log.info("Vehicle count retrieved for {}: {}", direction, count);
        return count;
    }

    public void incrementVehicleCount(Direction direction) {
        trafficRepository.incrementCount(direction);
        log.info("Vehicle count incremented for {}", direction);
    }

    public void resetVehicleCount(Direction direction) {
        trafficRepository.resetCount(direction);
        log.info("Vehicle count reset for {}", direction);
    }

    public VehicleCounter getVehicleCounter(Direction direction) {
        return trafficRepository.getCounter(direction);
    }

    public void detectTrafficCondition(Direction direction) {
        int count = trafficRepository.getCount(direction);
        if (count > 10) {
            log.info("High traffic detected for {}: {} vehicles", direction, count);
            log.debug("TODO: Trigger dynamic timing adjustment");
        } else if (count == 0) {
            log.info("No traffic detected for {}", direction);
            log.debug("TODO: Consider reducing green time for this direction");
        }
    }
}