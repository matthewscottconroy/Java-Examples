package com.patterns.strategy;

import java.util.List;

/**
 * Value object returned by every {@link RouteStrategy}.
 *
 * @param distanceKm    total distance in kilometres
 * @param durationMin   estimated travel time in minutes
 * @param directions    ordered turn-by-turn steps
 * @param strategyUsed  which algorithm produced this route
 */
public record Route(
        double distanceKm,
        int durationMin,
        List<String> directions,
        String strategyUsed) {

    @Override
    public String toString() {
        return String.format("Route[%s] %.1f km, ~%d min | %s",
                strategyUsed, distanceKm, durationMin, directions);
    }
}
