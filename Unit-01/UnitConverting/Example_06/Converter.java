/**
 * Converter holds the data needed to perform one conversion.
 */
public class Converter {

    private String fromUnit;
    private String toUnit;
    private double amount;
    private double conversionConstant;
    private boolean isMultiply;

    public Converter(String fromUnit, String toUnit, double amount, double conversionConstant, boolean isMultiply) {
        this.fromUnit = fromUnit;
        this.toUnit = toUnit;
        this.amount = amount;
        this.conversionConstant = conversionConstant;
        this.isMultiply = isMultiply;
    }

    public double convert() {
        if (isMultiply) {
            return amount * conversionConstant;
        } else {
            return amount / conversionConstant;
        }
    }

    public String getFromUnit() {
        return fromUnit;
    }

    public String getToUnit() {
        return toUnit;
    }

    public double getAmount() {
        return amount;
    }
}

