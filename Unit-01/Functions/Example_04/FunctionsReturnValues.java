public class FunctionsReturnValues {
    static double addTax(double price, double taxRate) {
        return price * (1.0 + taxRate);
    }

    public static void main(String[] args) {
        double coffee = 3.50;
        double total = addTax(coffee, 0.07);
        System.out.println("After tax: " + total);
    }
}

