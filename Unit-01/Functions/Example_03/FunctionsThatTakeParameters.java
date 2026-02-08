public class FunctionsThatTakeParameters {
    static void printItem(String name, double price) {
        System.out.println("Item: " + name);
        System.out.println("Price: " + price);
        System.out.println();
    }

    public static void main(String[] args) {
        System.out.println("=== Receipt ===");
        printItem("Coffee", 3.50);
        printItem("Bagel", 2.25);
        printItem("Banana", 0.79);
    }
}

