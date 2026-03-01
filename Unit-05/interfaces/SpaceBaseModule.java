public class SpaceBaseModule implements LifeSupportSystem {
    private double oxygenLevel;
    private double temperature;

    public SpaceBaseModule() {
        this.oxygenLevel = 20.5;
        this.temperature = 21.5;
    }

    @Override
    public void regulateOxygenLevel(double targetPercentage) {
        this.oxygenLevel = targetPercentage;
        System.out.println("Module O₂ adjusted to " + targetPercentage + "%");
    }

    @Override
    public void regulateTemperature(double targetCelsius) {
        this.temperature = targetCelsius;
        System.out.println("Module temperature adjusted to " + targetCelsius + "°C");
    }

    @Override
    public double getCurrentOxygenLevel() { return oxygenLevel; }
    @Override
    public double getCurrentTemperature() { return temperature; }

    @Override
    public boolean isStable() {
        return oxygenLevel >= 19.5 && oxygenLevel <= 22.5 &&
               temperature >= 19 && temperature <= 25;
    }
}

