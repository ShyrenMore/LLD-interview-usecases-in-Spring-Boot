package com.personal.lld.domain.state;

import com.personal.lld.domain.Ride;
import com.personal.lld.domain.RideStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class RideStateMachine {

    private final Map<RideStatus, Set<RideStatus>> allowedTransitions =
            new EnumMap<>(RideStatus.class);

    public RideStateMachine() {
        allowedTransitions.put(
                RideStatus.REQUESTED,
                EnumSet.of(
                        RideStatus.ASSIGNED,
                        RideStatus.CANCELLED));

        allowedTransitions.put(
                RideStatus.ASSIGNED,
                EnumSet.of(
                        RideStatus.ACCEPTED,
                        RideStatus.CANCELLED,
                        RideStatus.REQUESTED));

        allowedTransitions.put(
                RideStatus.ACCEPTED,
                EnumSet.of(
                        RideStatus.IN_PROGRESS,
                        RideStatus.CANCELLED));

        allowedTransitions.put(
                RideStatus.IN_PROGRESS,
                EnumSet.of(
                        RideStatus.COMPLETED,
                        RideStatus.CANCELLED));

        allowedTransitions.put(
                RideStatus.COMPLETED,
                EnumSet.noneOf(RideStatus.class));

        allowedTransitions.put(
                RideStatus.CANCELLED,
                EnumSet.noneOf(RideStatus.class));
    }

    public void transition(Ride ride, RideStatus nextStatus) {
        RideStatus current = ride.getStatus();

        Set<RideStatus> allowed =
                allowedTransitions.getOrDefault(
                        current,
                        EnumSet.noneOf(RideStatus.class));

        if (!allowed.contains(nextStatus)) {
            throw new IllegalStateException(
                    "Invalid ride state transition from "
                            + current + " to " + nextStatus);
        }

        ride.setStatus(nextStatus);
    }
}
