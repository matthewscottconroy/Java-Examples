package com.patterns.strategy;

import java.util.List;

/**
 * Strategy interface — each implementation defines a different routing algorithm.
 *
 * <p>The navigation app holds one {@code RouteStrategy} and delegates all
 * route-finding work to it. Swapping strategies at runtime changes the algorithm
 * without touching the app's code.
 */
public interface RouteStrategy {

    /**
     * Calculate a route between two points.
     *
     * @param origin      starting location label
     * @param destination ending location label
     * @return a {@link Route} with distance, time, and turn-by-turn directions
     */
    Route calculate(String origin, String destination);

    /** Short display name shown in the UI. */
    String name();
}
