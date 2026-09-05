package com.personal.lld.domain;

import com.personal.lld.domain.enums.Direction;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString(exclude = {"trafficLights", "emergencyDirection"})
public class Intersection {

    private final int id;
    private final String name;
    private final Map<Direction, TrafficLight> trafficLights = new HashMap<>();

    private boolean emergencyMode;
    private Direction emergencyDirection;
    private boolean cyclePaused;

    public Intersection(int id, String name) {
        this.id = id;
        this.name = name;

        for (Direction direction : Direction.values()) {
            this.trafficLights.put(direction, new TrafficLight(direction));
        }

        System.out.println("Intersection created: " + name + " (ID: " + id + ")");
    }


    // ---- Traffic light operations ----

    public TrafficLight getTrafficLight(Direction direction) {
        return trafficLights.get(direction);
    }

    public void setAllSignalsToRed() {
        for (TrafficLight light : trafficLights.values()) {
            switch (light.getCurrentState().getStateName()) {
                case "GREEN" -> {
                    light.turnYellow();
                    light.turnRed();
                }
                case "YELLOW" -> light.turnRed();
                case "RED" -> System.out.println("Traffic light " + light.getDirection() + " is already RED");
                default -> light.turnRed();
            }
        }
        System.out.println("All signals set to RED for intersection " + id);
    }

    public void setSignalToGreen(Direction direction) {
        TrafficLight light = trafficLights.get(direction);
        if (light != null) {
            light.turnGreen();
            System.out.println("Signal " + direction + " set to GREEN for intersection " + id);
        }
    }

    public void setSignalToYellow(Direction direction) {
        TrafficLight light = trafficLights.get(direction);
        if (light != null) {
            light.turnYellow();
            System.out.println("Signal " + direction + " set to YELLOW for intersection " + id);
        }
    }

    public void setSignalToRed(Direction direction) {
        TrafficLight light = trafficLights.get(direction);
        if (light != null) {
            light.turnRed();
            System.out.println("Signal " + direction + " set to RED for intersection " + id);
        }
    }

    public void setSignalToOff(Direction direction) {
        TrafficLight light = trafficLights.get(direction);
        if (light != null) {
            light.turnOff();
            System.out.println("Signal " + direction + " set to OFF for intersection " + id);
        }
    }

    /**
     * Emergency method to safely transition a signal to RED
     */
    public void emergencyTransitionToRed(Direction direction) {
        TrafficLight light = trafficLights.get(direction);
        if (light == null) return;

        switch (light.getCurrentState().getStateName()) {
            case "GREEN" -> {
                System.out.println("Emergency transition: " + direction + " GREEN → YELLOW → RED");
                light.turnYellow();
                light.turnRed();
            }
            case "YELLOW" -> {
                System.out.println("Emergency transition: " + direction + " YELLOW → RED");
                light.turnRed();
            }
            case "RED" -> System.out.println("Emergency transition: " + direction + " already RED");
            default -> {
                System.out.println("Emergency transition: " + direction + " → RED");
                light.turnRed();
            }
        }
    }
}
