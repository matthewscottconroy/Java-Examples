package com.patterns.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Subject (Observable) — the weather sensor that broadcasts measurements.
 *
 * <p>Any number of {@link WeatherObserver} subscribers register with the station.
 * When {@link #setMeasurements} is called (a new reading arrives from the sensor),
 * every registered observer is notified immediately.
 *
 * <p>Observers can register and unregister at any time without the station needing
 * to know what they do with the data.
 */
public class WeatherStation {

    private final List<WeatherObserver> observers = new ArrayList<>();

    private double temperatureC;
    private double humidityPct;
    private double pressureHPa;

    /**
     * Registers a new observer.
     *
     * @param observer the observer to add
     */
    public void subscribe(WeatherObserver observer) {
        observers.add(observer);
        System.out.println("Station: " + observer.getName() + " subscribed.");
    }

    /**
     * Removes a previously registered observer.
     *
     * @param observer the observer to remove
     */
    public void unsubscribe(WeatherObserver observer) {
        observers.remove(observer);
        System.out.println("Station: " + observer.getName() + " unsubscribed.");
    }

    /**
     * Updates the measurements and notifies all observers.
     *
     * @param temperatureC new temperature reading
     * @param humidityPct  new humidity reading
     * @param pressureHPa  new pressure reading
     */
    public void setMeasurements(double temperatureC, double humidityPct, double pressureHPa) {
        this.temperatureC = temperatureC;
        this.humidityPct  = humidityPct;
        this.pressureHPa  = pressureHPa;
        System.out.printf("%nStation: new reading → %.1f°C, %.0f%% humidity, %.0f hPa%n",
                temperatureC, humidityPct, pressureHPa);
        notifyObservers();
    }

    private void notifyObservers() {
        observers.forEach(o -> o.update(temperatureC, humidityPct, pressureHPa));
    }

    /** @return number of active subscribers */
    public int getSubscriberCount() { return observers.size(); }
}
