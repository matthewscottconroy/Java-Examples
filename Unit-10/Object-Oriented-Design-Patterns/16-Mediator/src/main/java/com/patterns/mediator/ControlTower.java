package com.patterns.mediator;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete Mediator — the airport control tower.
 *
 * <p>The tower tracks which runway is in use and which aircraft are in the
 * pattern. It is the only entity with a global view of traffic. Aircraft
 * hold a reference only to the tower, never to each other.
 *
 * <p><b>Pattern roles:</b>
 * <pre>
 *   AirTrafficControl — Mediator interface
 *   ControlTower      — Concrete Mediator
 *   Aircraft / Flight — Colleagues
 * </pre>
 */
public class ControlTower implements AirTrafficControl {

    private boolean runwayOccupied = false;
    private final List<String> trafficLog = new ArrayList<>();

    @Override
    public boolean requestClearance(Aircraft aircraft, String request) {
        String msg;
        boolean granted;

        switch (request.toUpperCase()) {
            case "TAKEOFF", "LAND" -> {
                if (runwayOccupied) {
                    msg = "[Tower] HOLD " + aircraft.getCallSign()
                            + " — runway occupied. " + request + " denied.";
                    granted = false;
                } else {
                    runwayOccupied = true;
                    msg = "[Tower] CLEARED " + aircraft.getCallSign()
                            + " for " + request + ".";
                    granted = true;
                }
            }
            case "TAXI" -> {
                msg = "[Tower] " + aircraft.getCallSign() + " cleared to taxi to runway.";
                granted = true;
            }
            default -> {
                msg = "[Tower] " + aircraft.getCallSign() + " — unknown request: " + request;
                granted = false;
            }
        }

        System.out.println(msg);
        trafficLog.add(msg);
        return granted;
    }

    @Override
    public void notifyComplete(Aircraft aircraft, String action) {
        if ("LANDED".equalsIgnoreCase(action) || "DEPARTED".equalsIgnoreCase(action)) {
            runwayOccupied = false;
        }
        String msg = "[Tower] Acknowledged " + aircraft.getCallSign() + ": " + action;
        System.out.println(msg);
        trafficLog.add(msg);
    }

    /** @return true if the runway is currently in use */
    public boolean isRunwayOccupied() { return runwayOccupied; }

    /** @return the full traffic log */
    public List<String> getTrafficLog() { return List.copyOf(trafficLog); }
}
