package com.patterns.strategy;

import java.util.List;

/** Strategy: minimise distance — cuts through side streets to shorten the odometer. */
public class ShortestRouteStrategy implements RouteStrategy {

    @Override
    public Route calculate(String origin, String destination) {
        return new Route(
                31.7,
                41,
                List.of(
                        "Head east on Oak Ave",
                        "Turn left onto Maple St",
                        "Continue through downtown",
                        "Turn right onto Elm Rd",
                        "Arrive at " + destination),
                name());
    }

    @Override
    public String name() { return "SHORTEST"; }
}
