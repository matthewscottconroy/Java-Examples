package com.patterns.strategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StrategyTest {

    @Test
    @DisplayName("FASTEST strategy returns a route with the shortest duration")
    void fastestHasShortestDuration() {
        NavigationApp nav = new NavigationApp(new FastestRouteStrategy());
        Route fastest = nav.navigate("A", "B");

        NavigationApp nav2 = new NavigationApp(new ShortestRouteStrategy());
        Route shortest = nav2.navigate("A", "B");

        assertTrue(fastest.durationMin() < shortest.durationMin(),
                "Fastest route should take less time than shortest route");
    }

    @Test
    @DisplayName("SHORTEST strategy returns a route with the least distance")
    void shortestHasLeastDistance() {
        Route shortest = new ShortestRouteStrategy().calculate("A", "B");
        Route fastest  = new FastestRouteStrategy().calculate("A", "B");
        assertTrue(shortest.distanceKm() < fastest.distanceKm());
    }

    @Test
    @DisplayName("Each strategy tags its route with its own name")
    void strategyNameTaggedOnRoute() {
        assertEquals("FASTEST", new FastestRouteStrategy().calculate("A", "B").strategyUsed());
        assertEquals("SHORTEST", new ShortestRouteStrategy().calculate("A", "B").strategyUsed());
        assertEquals("ECO",     new EcoFriendlyRouteStrategy().calculate("A", "B").strategyUsed());
    }

    @Test
    @DisplayName("Strategy can be swapped at runtime without changing the context")
    void runtimeSwap() {
        NavigationApp nav = new NavigationApp(new FastestRouteStrategy());
        assertEquals("FASTEST", nav.getStrategy().name());

        nav.setStrategy(new EcoFriendlyRouteStrategy());
        assertEquals("ECO", nav.getStrategy().name());

        Route route = nav.navigate("Home", "Work");
        assertEquals("ECO", route.strategyUsed());
    }

    @Test
    @DisplayName("All strategies produce non-empty directions")
    void allStrategiesHaveDirections() {
        RouteStrategy[] strategies = {
                new FastestRouteStrategy(),
                new ShortestRouteStrategy(),
                new EcoFriendlyRouteStrategy()
        };
        for (RouteStrategy s : strategies) {
            Route r = s.calculate("X", "Y");
            assertFalse(r.directions().isEmpty(),
                    s.name() + " produced no directions");
        }
    }
}
