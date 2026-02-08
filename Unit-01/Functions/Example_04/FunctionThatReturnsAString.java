public class FunctionThatReturnsAString {
    static String lineItem(String name, double price) {
        return name + " ... $" + price;
    }

    public static void main(String[] args) {
        System.out.println(lineItem("Coffee", 3.50));
        System.out.println(lineItem("Bagel", 2.25));
    }
}

