import java.io.*;
import java.nio.file.*;

public class P03_OpenFileInputStream {
    public static void main(String[] args) {
        Path p = Paths.get(args.length > 0 ? args[0] : "data.txt");

        try (InputStream in = Files.newInputStream(p)) {
            System.out.println("Opened for reading: " + p.toAbsolutePath());
            System.out.println("Available (hint only): " + in.available());
        } catch (IOException e) {
            System.err.println("Failed to open: " + e.getMessage());
        }
    }
}
