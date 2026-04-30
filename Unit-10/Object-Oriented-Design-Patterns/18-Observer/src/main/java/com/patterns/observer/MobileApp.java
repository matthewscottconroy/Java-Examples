package com.patterns.observer;

/** Concrete Observer — a user's mobile weather app. */
public class MobileApp implements WeatherObserver {

    private final String userName;

    public MobileApp(String userName) { this.userName = userName; }

    @Override
    public void update(double temp, double humidity, double pressure) {
        System.out.printf("  [MobileApp @%s] Updated: %.1f°C, feels like %.1f°C%n",
                userName, temp, feelsLike(temp, humidity));
    }

    @Override
    public String getName() { return "MobileApp(" + userName + ")"; }

    private double feelsLike(double t, double h) {
        return t - (0.2 * (100 - h) / 5.0); // simplified heat index
    }
}
