class Converter {

    private String unit;
    private double amount;
    private double conversionConstant;
    private boolean isMultiply;

    public Converter(String unit, double amount, double conversionConstant, boolean isMultiply) {
        this.unit = unit;
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

    public String getUnit() {
        return unit;
    }

    public double getAmount() {
        return amount;
    }
}

