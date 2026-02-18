import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import static java.nio.file.StandardCopyOption.*;

public class P08_RewriteFileWithChanges {
    public static void main(String[] args) throws IOException {
        Path input = Paths.get(args.length > 0 ? args[0] : "data.txt");
        Path dir = input.toAbsolutePath().getParent();
        if (dir == null) dir = Paths.get(".").toAbsolutePath();

        Path temp = Files.createTempFile(dir, "rewrite-", ".tmp");

        try (BufferedReader br = Files.newBufferedReader(input, StandardCharsets.UTF_8);
             BufferedWriter bw = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {

            String line;
            while ((line = br.readLine()) != null) {
                String changed = line.replace("foo", "bar").trim();
                if (changed.isEmpty()) continue; // drop blank lines
                bw.write(changed);
                bw.newLine();
            }
        }

        // Replace original with temp
        Files.move(temp, input, REPLACE_EXISTING, ATOMIC_MOVE);

        System.out.println("Rewrote with changes: " + input.toAbsolutePath());
    }
}
