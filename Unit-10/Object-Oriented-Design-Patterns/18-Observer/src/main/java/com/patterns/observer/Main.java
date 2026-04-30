package com.patterns.observer;

/**
 * Demonstrates the Observer pattern with a weather station.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Weather Station (Observer Pattern) ===\n");

        WeatherStation station = new WeatherStation();

        // Three different systems subscribe
        MobileApp       app        = new MobileApp("alice");
        SmartThermostat thermostat = new SmartThermostat();
        WeatherWebsite  website    = new WeatherWebsite();

        station.subscribe(app);
        station.subscribe(thermostat);
        station.subscribe(website);

        // New reading — all three notified simultaneously
        station.setMeasurements(18.5, 72, 1015);
        station.setMeasurements(24.0, 58, 1008);

        // Alice unsubscribes from weather alerts (she's on holiday)
        System.out.println();
        station.unsubscribe(app);

        // Next reading only goes to thermostat and website
        station.setMeasurements(10.0, 90, 998);

        System.out.println("\nActive subscribers: " + station.getSubscriberCount());
    }
}
