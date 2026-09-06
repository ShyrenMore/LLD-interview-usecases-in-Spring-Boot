package com.personal.lld.domain.strategy;

import java.util.List;

import com.personal.lld.domain.*;

public interface ElevatorSelectionStrategy {
    Elevator selectElevator(ExternalRequest r, List<Elevator> elevators);
}
