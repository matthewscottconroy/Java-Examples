import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

public class P08_Rethrow {

    static String loadConfig(Path path) throws IOException {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            // Add context, then rethrow (same exception object).
            System.err.println("While loading config at: " + path.toAbsolutePath());
            throw e;
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println(loadConfig(Path.of("config.txt")));
        } catch (IOException e) {
            System.out.println("Main decided to handle it here: " + e.getMessage());
        }
    }
}
