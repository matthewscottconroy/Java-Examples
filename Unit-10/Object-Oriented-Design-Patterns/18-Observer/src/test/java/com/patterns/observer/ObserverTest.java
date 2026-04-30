package com.patterns.observer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Observer pattern — Weather Station.
 */
class ObserverTest {

    /** Capture observer that records each update it receives. */
    static class CapturingObserver implements WeatherObserver {
        final List<Double> temps = new ArrayList<>();

        @Override
        public void update(double temp, double h, double p) { temps.add(temp); }

        @Override
        public String getName() { return "Capturing"; }
    }

    @Test
    @DisplayName("Observer receives update when station measurement changes")
    void observerReceivesUpdate() {
        WeatherStation station = new WeatherStation();
        CapturingObserver obs = new CapturingObserver();
        station.subscribe(obs);
        station.setMeasurements(20.0, 60, 1013);
        assertEquals(1, obs.temps.size());
        assertEquals(20.0, obs.temps.get(0));
    }

    @Test
    @DisplayName("Multiple observers all receive the same update")
    void multipleObserversNotified() {
        WeatherStation station = new WeatherStation();
        CapturingObserver a = new CapturingObserver();
        CapturingObserver b = new CapturingObserver();
        station.subscribe(a);
        station.subscribe(b);
        station.setMeasurements(15.0, 70, 1010);
        assertEquals(1, a.temps.size());
        assertEquals(1, b.temps.size());
    }

    @Test
    @DisplayName("Unsubscribed observer does not receive further updates")
    void unsubscribedObserverSilenced() {
        WeatherStation station = new WeatherStation();
        CapturingObserver obs = new CapturingObserver();
        station.subscribe(obs);
        station.setMeasurements(10.0, 80, 1005);
        station.unsubscribe(obs);
        station.setMeasurements(12.0, 75, 1007); // obs should not receive this
        assertEquals(1, obs.temps.size(), "Should only have received the first update");
    }

    @Test
    @DisplayName("Subscriber count reflects subscribe/unsubscribe operations")
    void subscriberCount() {
        WeatherStation station = new WeatherStation();
        CapturingObserver a = new CapturingObserver();
        CapturingObserver b = new CapturingObserver();
        station.subscribe(a);
        station.subscribe(b);
        assertEquals(2, station.getSubscriberCount());
        station.unsubscribe(a);
        assertEquals(1, station.getSubscriberCount());
    }
}
