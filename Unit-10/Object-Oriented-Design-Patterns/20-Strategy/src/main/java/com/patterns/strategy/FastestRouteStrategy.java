package com.patterns.strategy;

import java.util.List;

/** Strategy: minimise travel time — prefers motorways even when longer in distance. */
public class FastestRouteStrategy implements RouteStrategy {

    @Override
    public Route calculate(String origin, String destination) {
        return new Route(
                42.3,
                28,
                List.of(
                        "Head north on Main St",
                        "Merge onto I-90 West",
                        "Take exit 14B toward " + destination,
                        "Arrive at " + destination),
                name());
    }

    @Override
    public String name() { return "FASTEST"; }
}
