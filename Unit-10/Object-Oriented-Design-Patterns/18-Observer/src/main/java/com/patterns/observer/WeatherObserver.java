package com.patterns.observer;

/**
 * Observer interface — any system that wants to be notified of weather changes.
 */
public interface WeatherObserver {

    /**
     * Called by the weather station when conditions change.
     *
     * @param temperatureC the new temperature in degrees Celsius
     * @param humidityPct  the new relative humidity percentage
     * @param pressureHPa  the barometric pressure in hPa
     */
    void update(double temperatureC, double humidityPct, double pressureHPa);

    /** @return the observer's display name */
    String getName();
}
