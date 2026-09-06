package com.personal.lld.domain.strategy;

import java.util.*;
import java.util.stream.*;

import com.personal.lld.domain.*;

public class FCFSStrategy implements MovementStrategy {
    public List<Integer> calculatePath(Elevator e, List<InternalRequest> rs) {
        return rs.stream().sorted(Comparator.comparingLong(InternalRequest::getTimestamp)).map(InternalRequest::getDestinationFloor).distinct().toList();
    }
}
