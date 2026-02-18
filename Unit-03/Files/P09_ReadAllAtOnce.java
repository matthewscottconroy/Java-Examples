import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class P09_ReadAllAtOnce {
    public static void main(String[] args) throws IOException {
        Path p = Paths.get(args.length > 0 ? args[0] : "data.txt");

        String content = Files.readString(p, StandardCharsets.UTF_8);
        System.out.println("=== Entire file ===");
        System.out.print(content);
    }
}
