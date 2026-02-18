import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import static java.nio.file.StandardOpenOption.*;

public class P05_AppendToFile {
    public static void main(String[] args) throws IOException {
        Path p = Paths.get(args.length > 0 ? args[0] : "log.txt");

        String msg = Instant.now() + " - hello append\n";
        Files.write(p, msg.getBytes(StandardCharsets.UTF_8), CREATE, WRITE, APPEND);

        System.out.println("Appended to: " + p.toAbsolutePath());
    }
}
