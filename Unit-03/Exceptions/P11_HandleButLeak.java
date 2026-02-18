import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class P11_HandleButLeak {
    public static void main(String[] args) {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader("data.txt"));
            System.out.println("First line: " + br.readLine());

            // Force an exception after opening the resource:
            int x = 1 / 0;

            System.out.println("Never reached: " + x);
        } catch (IOException e) {
            System.out.println("I/O problem: " + e.getMessage());
        } catch (ArithmeticException e) {
            System.out.println("Math problem: " + e.getMessage());
            // BUG: we handled the exception, but we might not close br.
            // If data.txt existed, br is still open here.
        }

        System.out.println("Program ends (maybe with a leaked open file handle).");
    }
}
