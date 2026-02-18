import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

public class P09_WrapAndRethrow {

    static String loadTextOrCrash(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            // Wrap checked exception inside an unchecked exception.
            throw new RuntimeException("Failed to read required file: " + path, e);
        }
    }

    public static void main(String[] args) {
        // No try/catch here. If missing, RuntimeException will crash the program.
        System.out.println(loadTextOrCrash(Path.of("missing.txt")));
    }
}
