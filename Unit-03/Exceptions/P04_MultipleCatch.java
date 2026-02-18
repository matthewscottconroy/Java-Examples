import java.util.Scanner;

public class P04_MultipleCatch {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try {
            System.out.print("Enter an integer: ");
            int n = Integer.parseInt(sc.nextLine());   // could throw NumberFormatException

            System.out.print("Enter a divisor: ");
            int d = Integer.parseInt(sc.nextLine());   // could throw NumberFormatException

            System.out.println("Result = " + (n / d)); // could throw ArithmeticException
        } catch (NumberFormatException e) {
            System.out.println("That wasn't a valid integer. " + e);
        } catch (ArithmeticException e) {
            System.out.println("You divided by zero. " + e);
        } finally {
            // (We are intentionally NOT closing scanner here yet; we’ll handle resource closing later.)
            System.out.println("Done (finally).");
        }
    }
}
