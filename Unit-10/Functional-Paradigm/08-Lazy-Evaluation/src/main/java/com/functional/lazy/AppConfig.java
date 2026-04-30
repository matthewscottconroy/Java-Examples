package com.functional.lazy;

import java.util.Map;

/**
 * Application configuration that loads expensive sub-configurations lazily.
 *
 * <p>The database URL, API key, and feature-flag map are each wrapped in a
 * {@link Lazy}. They are initialised only when first accessed, not when the
 * {@code AppConfig} object is constructed. This matters at startup: you pay
 * only for the configuration sections you actually use in a given run.
 */
public class AppConfig {

    private final Lazy<String>              dbUrl;
    private final Lazy<String>              apiKey;
    private final Lazy<Map<String, Boolean>> featureFlags;

    public AppConfig() {
        dbUrl = Lazy.of(() -> {
            System.out.println("  [Lazy] Loading database URL from environment…");
            simulateSlowLoad(50);
            return "jdbc:postgresql://db.internal:5432/prod";
        });

        apiKey = Lazy.of(() -> {
            System.out.println("  [Lazy] Decrypting API key from secrets vault…");
            simulateSlowLoad(80);
            return "sk-live-a3f9b2c1d4e5";
        });

        featureFlags = Lazy.of(() -> {
            System.out.println("  [Lazy] Fetching feature flags from config service…");
            simulateSlowLoad(120);
            return Map.of(
                    "new-checkout-flow", true,
                    "dark-mode",         true,
                    "beta-search",       false
            );
        });
    }

    public String       getDatabaseUrl()  { return dbUrl.get(); }
    public String       getApiKey()       { return apiKey.get(); }
    public Map<String, Boolean> getFeatureFlags() { return featureFlags.get(); }

    public boolean isDatabaseUrlLoaded() { return dbUrl.isComputed(); }
    public boolean isApiKeyLoaded()      { return apiKey.isComputed(); }
    public boolean isFeatureFlagsLoaded(){ return featureFlags.isComputed(); }

    private static void simulateSlowLoad(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
