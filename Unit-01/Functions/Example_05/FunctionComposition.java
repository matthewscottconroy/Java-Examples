public class FunctionComposition {
    static double addTax(double price, double taxRate) {
        return price * (1.0 + taxRate);
    }

    static String lineItemWithTax(String name, double price, double taxRate) {
        double taxed = addTax(price, taxRate);
        return name + " after tax: $" + taxed;
    }

    public static void main(String[] args) {
        System.out.println(lineItemWithTax("Coffee", 3.50, 0.07));
        System.out.println(lineItemWithTax("Bagel", 2.25, 0.07));
    }
}

