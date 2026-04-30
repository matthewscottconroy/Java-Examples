import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

// P13 shows the Java 7+ solution to the P11/P12 resource-leak problem.
// try-with-resources automatically calls close() on every declared resource
// at the end of the block — whether it exits normally, via exception, or via return.
// The verbose null-check + nested try-catch in P12's finally block is no longer needed.
public class P13_TryWithResources {
    public static void main(String[] args) {
        // Any object whose class implements AutoCloseable (or Closeable) can be
        // declared here. Multiple resources are separated by semicolons and
        // closed in reverse declaration order.
        try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
            System.out.println("First line: " + br.readLine());

            // Intentionally trigger an exception after the resource is open.
            int x = 1 / 0;
            System.out.println("Never reached: " + x);

        } catch (IOException e) {
            System.out.println("I/O problem: " + e.getMessage());
        } catch (ArithmeticException e) {
            System.out.println("Math problem: " + e.getMessage());
            // br is guaranteed to be closed here — no finally block needed.
        }

        System.out.println("Program continues cleanly (no leaked file handle).");
    }
}
