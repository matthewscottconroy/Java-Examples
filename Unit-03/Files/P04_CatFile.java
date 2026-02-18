import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class P04_CatFile {
    public static void main(String[] args) throws IOException {
        Path p = Paths.get(args.length > 0 ? args[0] : "data.txt");

        try (BufferedReader br = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }
    }
}
