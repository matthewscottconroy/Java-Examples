public interface LifeSupportSystem {
    void regulateOxygenLevel(double targetPercentage);
    void regulateTemperature(double targetCelsius);
    double getCurrentOxygenLevel();
    double getCurrentTemperature();
    boolean isStable(); // true if all readings within safe range
}

