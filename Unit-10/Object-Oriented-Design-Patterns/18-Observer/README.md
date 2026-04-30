# 18 — Observer: The Weather Station

## The Story

A weather sensor on the roof of a building takes readings every minute. When the
temperature drops below freezing, three very different systems need to know:

- **The mobile app** shows the user "feels like -3°C — wear a coat"
- **The smart thermostat** switches from HOLD to HEAT mode
- **The weather website** updates its widget from "Cloudy" to "Freezing"

The sensor doesn't know any of these systems exist. It just broadcasts its reading.
Each system subscribes at startup and does whatever it wants with the data.

Add a fourth subscriber — an irrigation system that cancels watering when it rains
— and you add one line to the startup code. The sensor changes nothing.

This is the **Observer** pattern: define a one-to-many dependency so that when one
object changes state, all its dependents are notified automatically.

---

## The Problem It Solves

Without Observer, the weather sensor would need to call every interested system
by name: `app.update(...)`, `thermostat.update(...)`, `website.update(...)`. Adding
a new subscriber means editing the sensor. The sensor becomes coupled to everything.

With Observer, the sensor only knows about a list of abstract `WeatherObserver`
subscribers. It iterates and calls `update()`. New subscribers are added and removed
at runtime without touching the sensor.

---

## Structure

```
WeatherObserver     ← Observer interface (update)
  ├── MobileApp
  ├── SmartThermostat
  └── WeatherWebsite

WeatherStation      ← Subject / Observable (subscribe, unsubscribe, setMeasurements)
```

---

## When to Use It

| Situation | Example |
|-----------|---------|
| One event, many listeners | GUI event handling, stock tickers, news feeds |
| Loose coupling between source and consumers | Sensor → multiple systems |
| Dynamic subscribe/unsubscribe at runtime | User preferences, plugin systems |

Java's standard library uses this pattern: `java.util.Observable` (legacy),
`PropertyChangeListener`, and all modern reactive streams.

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```
