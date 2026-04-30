package com.functional.async;

/**
 * Aggregated data shown on a user's dashboard.
 *
 * @param weather     current weather summary
 * @param stockPrice  portfolio value in dollars
 * @param headline    top news headline
 */
public record DashboardData(String weather, double stockPrice, String headline) {

    @Override
    public String toString() {
        return String.format(
                "Dashboard { weather='%s', portfolio=$%.2f, headline='%s' }",
                weather, stockPrice, headline);
    }
}
