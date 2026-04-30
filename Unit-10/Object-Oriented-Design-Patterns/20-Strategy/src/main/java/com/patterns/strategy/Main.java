package com.patterns.strategy;

/**
 * Demonstrates the Strategy pattern with a navigation app.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Route Planner (Strategy Pattern) ===\n");

        NavigationApp nav = new NavigationApp(new FastestRouteStrategy());

        nav.navigate("Home", "Airport");

        System.out.println();
        nav.setStrategy(new ShortestRouteStrategy());
        nav.navigate("Home", "Airport");

        System.out.println();
        nav.setStrategy(new EcoFriendlyRouteStrategy());
        nav.navigate("Home", "Airport");

        System.out.println("\n--- Same destination, three different journeys ---");
        System.out.println("The NavigationApp code never changed. Only the strategy did.");
    }
}
