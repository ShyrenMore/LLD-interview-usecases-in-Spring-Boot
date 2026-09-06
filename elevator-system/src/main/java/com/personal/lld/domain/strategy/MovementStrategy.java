package com.personal.lld.domain.strategy;

import java.util.List;

import com.personal.lld.domain.*;

public interface MovementStrategy {
    List<Integer> calculatePath(Elevator elevator, List<InternalRequest> requests);
}
