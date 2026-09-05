package com.personal.lld.domain.state;

import com.personal.lld.domain.TrafficLight;

public class OffState implements TrafficLightState {

    @Override
    public void turnGreen(TrafficLight trafficLight) {
        // Valid transition: OFF → GREEN
        trafficLight.setCurrentState(new GreenState());
        System.out.println("Traffic light " + trafficLight.getDirection() + " changed from OFF to GREEN");
    }

    @Override
    public void turnYellow(TrafficLight trafficLight) {
        // Valid transition: OFF → YELLOW
        trafficLight.setCurrentState(new YellowState());
        System.out.println("Traffic light " + trafficLight.getDirection() + " changed from OFF to YELLOW");
    }

    @Override
    public void turnRed(TrafficLight trafficLight) {
        // Valid transition: OFF → RED
        trafficLight.setCurrentState(new RedState());
        System.out.println("Traffic light " + trafficLight.getDirection() + " changed from OFF to RED");
    }

    @Override
    public void turnOff(TrafficLight trafficLight) {
        // No change - already OFF
        System.out.println("Traffic light " + trafficLight.getDirection() + " is already OFF");
    }

    @Override
    public String getStateName() {
        return "OFF";
    }

    @Override
    public boolean canTransitionTo(TrafficLightState newState) {
        return newState instanceof RedState || newState instanceof GreenState || newState instanceof YellowState;
    }
}