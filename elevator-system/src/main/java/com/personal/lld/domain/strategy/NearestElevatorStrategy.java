package com.personal.lld.domain.strategy;

import java.util.*;

import com.personal.lld.domain.*;

public class NearestElevatorStrategy implements ElevatorSelectionStrategy {
    public Elevator selectElevator(ExternalRequest r, List<Elevator> es) {
        Elevator best = null;
        int d = Integer.MAX_VALUE;
        for (Elevator e : es) {
            if (!e.isAvailable()) continue;
            int x = Math.abs(e.getCurrentFloor() - r.getFloorNumber());
            if (x < d) {
                d = x;
                best = e;
            }
        }
        return best;
    }
}
