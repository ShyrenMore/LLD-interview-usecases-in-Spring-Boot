package com.personal.lld.domain.strategy;

import java.util.*;

import com.personal.lld.domain.*;

public class ScanStrategy implements MovementStrategy {
    public List<Integer> calculatePath(Elevator e, List<InternalRequest> rs) {
        List<Integer> f = rs.stream().map(InternalRequest::getDestinationFloor).distinct().sorted().toList();
        if (f.isEmpty()) return List.of();
        int c = e.getCurrentFloor();
        Direction d = e.getDirection();
        if (d == Direction.IDLE) d = f.get(0) >= c ? Direction.UP : Direction.DOWN;
        List<Integer> out = new ArrayList<>();
        if (d == Direction.UP) {
            f.stream().filter(x -> x >= c).forEach(out::add);
            f.stream().filter(x -> x < c).sorted(Comparator.reverseOrder()).forEach(out::add);
        } else {
            f.stream().filter(x -> x <= c).sorted(Comparator.reverseOrder()).forEach(out::add);
            f.stream().filter(x -> x > c).forEach(out::add);
        }
        return out;
    }
}
