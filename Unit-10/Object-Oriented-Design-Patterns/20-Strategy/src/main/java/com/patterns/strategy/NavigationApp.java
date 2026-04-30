package com.patterns.strategy;

/**
 * Context — the navigation app.
 *
 * <p>It holds a {@link RouteStrategy} and delegates route calculation to it.
 * The user can switch strategies at any time without restarting the app.
 */
public class NavigationApp {

    private RouteStrategy strategy;

    public NavigationApp(RouteStrategy strategy) {
        this.strategy = strategy;
    }

    /** Switch routing mode at runtime. */
    public void setStrategy(RouteStrategy strategy) {
        System.out.println("[Nav] Switching to " + strategy.name() + " mode.");
        this.strategy = strategy;
    }

    /**
     * Calculate and display a route using the currently selected strategy.
     */
    public Route navigate(String origin, String destination) {
        System.out.printf("[Nav] Calculating %s route from %s to %s…%n",
                strategy.name(), origin, destination);
        Route route = strategy.calculate(origin, destination);
        System.out.printf("      %.1f km, ~%d min%n", route.distanceKm(), route.durationMin());
        route.directions().forEach(step -> System.out.println("      → " + step));
        return route;
    }

    public RouteStrategy getStrategy() { return strategy; }
}
