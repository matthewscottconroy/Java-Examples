package com.patterns.observer;

/** Concrete Observer — a smart thermostat that adjusts HVAC based on outdoor conditions. */
public class SmartThermostat implements WeatherObserver {

    private static final double TARGET_C = 21.0;

    @Override
    public void update(double temp, double humidity, double pressure) {
        String action = temp < TARGET_C ? "HEAT" : temp > TARGET_C + 2 ? "COOL" : "HOLD";
        System.out.printf("  [Thermostat] Outdoor %.1f°C → HVAC mode: %s%n", temp, action);
    }

    @Override
    public String getName() { return "SmartThermostat"; }
}
