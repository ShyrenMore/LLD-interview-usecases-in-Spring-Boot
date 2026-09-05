package com.personal.lld.domain;

import com.personal.lld.domain.enums.Direction;
import com.personal.lld.domain.state.TrafficLightState;
import com.personal.lld.domain.state.RedState;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class TrafficLight {

    private Direction direction;
    private TrafficLightState currentState;

    public TrafficLight(Direction direction) {
        this.direction = direction;
        this.currentState = new RedState(); // Start with RED state
        log.info("Traffic light created for direction: {} in RED state", direction);
    }

    public void turnGreen() {
        currentState.turnGreen(this);
    }

    public void turnYellow() {
        currentState.turnYellow(this);
    }

    public void turnRed() {
        currentState.turnRed(this);
    }

    public void turnOff() {
        currentState.turnOff(this);
    }

    public String getCurrentStateName() {
        return currentState != null ? currentState.getStateName() : "UNKNOWN";
    }

    public boolean canTransitionTo(TrafficLightState newState) {
        return currentState != null && currentState.canTransitionTo(newState);
    }

    @Override
    public String toString() {
        return "TrafficLight{" +
                "direction=" + direction +
                ", state=" + getCurrentStateName() +
                '}';
    }
}