import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import static java.nio.file.StandardOpenOption.*;

public class P06_OverwriteFile {
    public static void main(String[] args) throws IOException {
        Path p = Paths.get(args.length > 0 ? args[0] : "data.txt");

        String content = "This replaces the entire file.\nLine 2.\n";
        Files.write(p, content.getBytes(StandardCharsets.UTF_8), CREATE, WRITE, TRUNCATE_EXISTING);

        System.out.println("Overwrote: " + p.toAbsolutePath());
    }
}
