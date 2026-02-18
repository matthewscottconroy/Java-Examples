import java.io.IOException;
import java.nio.file.*;

public class P07_DeleteFile {
    public static void main(String[] args) throws IOException {
        Path p = Paths.get(args.length > 0 ? args[0] : "to_delete.txt");

        boolean deleted = Files.deleteIfExists(p);
        System.out.println((deleted ? "Deleted: " : "Did not exist: ") + p.toAbsolutePath());
    }
}
