public class Temperature {
    private double celsius;

    public Temperature(double c) {
        celsius = c;
    }

    public double toFahrenheit() {
        return celsius * 9 / 5 + 32;
    }

    public void increase(double amount) {
        celsius += amount;
    }
}

