public class AstronautSuit implements LifeSupportSystem {
    private double oxygenLevel;
    private double temperature;

    public AstronautSuit() {
        this.oxygenLevel = 21.0; // normal Earth percentage
        this.temperature = 22.0; // Celsius
    }

    @Override
    public void regulateOxygenLevel(double targetPercentage) {
        System.out.println("Adjusting oxygen to " + targetPercentage + "%...");
        this.oxygenLevel = targetPercentage;
    }

    @Override
    public void regulateTemperature(double targetCelsius) {
        System.out.println("Adjusting temperature to " + targetCelsius + "°C...");
        this.temperature = targetCelsius;
    }

    @Override
    public double getCurrentOxygenLevel() {
        return oxygenLevel;
    }

    @Override
    public double getCurrentTemperature() {
        return temperature;
    }

    @Override
    public boolean isStable() {
        return oxygenLevel >= 19.5 && oxygenLevel <= 23.5 &&
               temperature >= 18 && temperature <= 26;
    }

    public void displayStatus() {
        System.out.println("O₂: " + oxygenLevel + "% | Temp: " + temperature + "°C | Stable: " + isStable());
    }
}

