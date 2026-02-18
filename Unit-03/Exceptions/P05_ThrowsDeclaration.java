import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

public class P05_ThrowsDeclaration {

    // This method does NOT handle IOException; it declares it.
    static String loadTextFile(Path path) throws IOException {
        return Files.readString(path);
    }

    public static void main(String[] args) {
        try {
            String content = loadTextFile(Path.of("missing.txt"));
            System.out.println(content);
        } catch (IOException e) {
            System.out.println("Could not read file: " + e.getMessage());
        }
    }
}
