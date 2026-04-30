package com.patterns.strategy;

import java.util.List;

/**
 * Strategy: minimise fuel/emissions — favours steady-speed roads and avoids stop-start traffic.
 */
public class EcoFriendlyRouteStrategy implements RouteStrategy {

    @Override
    public Route calculate(String origin, String destination) {
        return new Route(
                38.1,
                35,
                List.of(
                        "Head south on Green Blvd (45 km/h zone)",
                        "Join Riverside Parkway (steady 60 km/h)",
                        "Exit at Park & Ride interchange",
                        "Arrive at " + destination + " (EV-charging available)"),
                name());
    }

    @Override
    public String name() { return "ECO"; }
}
