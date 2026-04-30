package com.functional.lazy;

/**
 * Demonstrates lazy evaluation with a configuration loader.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Configuration Loader (Lazy Evaluation) ===\n");

        System.out.println("Creating AppConfig… (fast — nothing is loaded yet)");
        AppConfig config = new AppConfig();

        System.out.println("DB loaded? "    + config.isDatabaseUrlLoaded());
        System.out.println("API loaded? "   + config.isApiKeyLoaded());
        System.out.println("Flags loaded? " + config.isFeatureFlagsLoaded());

        System.out.println("\nA request comes in that needs the database:");
        String url = config.getDatabaseUrl();
        System.out.println("  → " + url);

        System.out.println("\nA second request needs the same URL (cached):");
        String url2 = config.getDatabaseUrl(); // no re-computation
        System.out.println("  → " + url2 + "  (same object: " + (url == url2) + ")");

        System.out.println("\nDB loaded? "    + config.isDatabaseUrlLoaded());
        System.out.println("API loaded? "   + config.isApiKeyLoaded());   // still false
        System.out.println("Flags loaded? " + config.isFeatureFlagsLoaded()); // still false

        System.out.println("\nA request checks a feature flag:");
        boolean betaSearch = config.getFeatureFlags().getOrDefault("beta-search", false);
        System.out.println("  beta-search enabled: " + betaSearch);

        System.out.println("\nFinal state:");
        System.out.println("  DB loaded? "    + config.isDatabaseUrlLoaded());
        System.out.println("  API loaded? "   + config.isApiKeyLoaded());
        System.out.println("  Flags loaded? " + config.isFeatureFlagsLoaded());
    }
}
