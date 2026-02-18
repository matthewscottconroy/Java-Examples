import java.io.IOException;
import java.nio.file.*;

public class P02_CreateFile {
    public static void main(String[] args) throws IOException {
        Path p = Paths.get(args.length > 0 ? args[0] : "created.txt");

        System.out.println("Creating: " + p.toAbsolutePath());
        Files.createFile(p); // throws if already exists
        System.out.println("Done.");
    }
}
