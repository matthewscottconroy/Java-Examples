import java.nio.file.*;

public class P01_FileExists {
    public static void main(String[] args) {
        Path p = Paths.get(args.length > 0 ? args[0] : "data.txt");

        System.out.println("Path: " + p.toAbsolutePath());
        System.out.println("exists?      " + Files.exists(p));
        System.out.println("isRegular?   " + Files.isRegularFile(p));
        System.out.println("isDirectory? " + Files.isDirectory(p));
    }
}
