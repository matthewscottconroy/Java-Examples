public class P01_Unhandled {
    public static void main(String[] args) {
        int x = 10;
        int y = 0;

        // No try/catch: the exception bubbles up to the JVM and crashes the program.
        int z = x / y;

        // This line is never reached.
        System.out.println("z = " + z);
    }
}
