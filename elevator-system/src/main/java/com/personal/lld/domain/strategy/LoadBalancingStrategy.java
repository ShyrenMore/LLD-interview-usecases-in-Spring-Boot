package com.personal.lld.domain.strategy;

import java.util.*;

import com.personal.lld.domain.*;

public class LoadBalancingStrategy implements ElevatorSelectionStrategy {
    public Elevator selectElevator(ExternalRequest r, List<Elevator> es) {
        return es.stream().filter(Elevator::isAvailable).min(Comparator.comparingInt(Elevator::getCurrentLoad).thenComparingInt(e -> Math.abs(e.getCurrentFloor() - r.getFloorNumber()))).orElse(null);
    }
}
