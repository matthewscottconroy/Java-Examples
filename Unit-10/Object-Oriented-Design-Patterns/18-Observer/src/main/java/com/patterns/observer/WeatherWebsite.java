package com.patterns.observer;

/** Concrete Observer — a public weather website widget. */
public class WeatherWebsite implements WeatherObserver {

    @Override
    public void update(double temp, double humidity, double pressure) {
        String condition = pressure > 1013 ? "Sunny" : pressure < 1000 ? "Rainy" : "Cloudy";
        System.out.printf("  [WeatherWebsite] %.1f°C, %s (%.0f hPa)%n",
                temp, condition, pressure);
    }

    @Override
    public String getName() { return "WeatherWebsite"; }
}
