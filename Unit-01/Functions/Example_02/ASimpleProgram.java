public class ASimpleProgram {
    static void printReceiptHeader() {
        System.out.println("=== Receipt ===");
    }

    public static void main(String[] args) {
        printReceiptHeader();
        System.out.println("Item: Coffee");
        System.out.println("Price: 3.50");
        System.out.println();

        printReceiptHeader();
        System.out.println("Item: Bagel");
        System.out.println("Price: 2.25");
        System.out.println();
    }
}

