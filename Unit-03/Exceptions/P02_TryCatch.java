public class P02_TryCatch {
    public static void main(String[] args) {
        int x = 10;
        int y = 0;

        try {
            int z = x / y;
            System.out.println("z = " + z); // not reached
        } catch (ArithmeticException e) {
            System.out.println("Caught: " + e);
        }

        System.out.println("Program continues after catch.");
    }
}
