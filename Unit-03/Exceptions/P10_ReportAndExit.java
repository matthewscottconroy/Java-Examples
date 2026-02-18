import java.nio.file.Path;

public class P10_ReportAndExit {

    public static void main(String[] args) {
        try {
            // reuse from P09 concept: may throw RuntimeException
            String content = P09_WrapAndRethrow.loadTextOrCrash(Path.of("missing.txt"));
            System.out.println(content);
        } catch (RuntimeException e) {
            System.err.println("FATAL: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }

        // Typically unreachable if fatal.
        System.out.println("Continuing...");
    }

    // Included here so this file is standalone.
    static class P09_WrapAndRethrow {
        static String loadTextOrCrash(Path path) {
            try {
                return java.nio.file.Files.readString(path);
            } catch (java.io.IOException e) {
                throw new RuntimeException("Failed to read required file: " + path, e);
            }
        }
    }
}
