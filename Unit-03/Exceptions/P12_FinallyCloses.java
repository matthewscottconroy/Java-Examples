import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class P12_FinallyCloses {
    public static void main(String[] args) {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader("data.txt"));
            System.out.println("First line: " + br.readLine());

            // Force an exception after opening:
            int x = 1 / 0;
            System.out.println("Never reached: " + x);
        } catch (IOException e) {
            System.out.println("I/O problem: " + e.getMessage());
        } catch (ArithmeticException e) {
            System.out.println("Math problem: " + e.getMessage());
        } finally {
            // Cleanup belongs here if you're not using try-with-resources.
            if (br != null) {
                try {
                    br.close();
                    System.out.println("Closed file in finally.");
                } catch (IOException closeErr) {
                    System.out.println("Failed to close: " + closeErr.getMessage());
                }
            }
        }

        System.out.println("Program continues safely.");
    }
}
