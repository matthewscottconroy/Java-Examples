package com.io.nio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.List;

/**
 * The {@link Files} utility class — NIO.2 (Java 7+).
 *
 * <p>For most file operations you no longer need to open streams manually.
 * {@code Files} wraps the common operations in one-liners that handle
 * resource management internally.
 *
 * <p>The {@link Path} interface replaces the old {@link java.io.File} class:
 * it is more expressive, supports symbolic links properly, and integrates
 * cleanly with the rest of {@code java.nio.file}.
 *
 * <p>Obtain a Path from:
 * <ul>
 *   <li>{@code Path.of("/some/path")} or {@code Paths.get("/some/path")} (same thing)</li>
 *   <li>{@code path.resolve("child")} — append a relative path</li>
 *   <li>{@code path.resolveSibling("sibling")} — replace the last component</li>
 * </ul>
 */
public class FilesDemo {

    // -----------------------------------------------------------------------
    // Common read / write operations
    // -----------------------------------------------------------------------
    public static void showReadWrite(Path tempDir) throws IOException {
        System.out.println("-- Files read/write shortcuts --");
        Path file = tempDir.resolve("nio.txt");

        // Write a complete string in one call — always with an explicit charset.
        Files.writeString(file, "First line\nSecond line\nThird line",
                StandardCharsets.UTF_8);
        System.out.println("  wrote: " + Files.size(file) + " bytes");

        // Read the whole file as a String.
        String content = Files.readString(file, StandardCharsets.UTF_8);
        System.out.println("  readString: " + content.replace("\n", " | "));

        // Read as a List<String> — one element per line.
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        System.out.println("  readAllLines: " + lines);

        // Append to an existing file.
        Files.writeString(file, "\nFourth line",
                StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        System.out.println("  after append: " + Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    // -----------------------------------------------------------------------
    // Copy, move, delete
    // -----------------------------------------------------------------------
    public static void showCopyMoveDelete(Path tempDir) throws IOException {
        System.out.println("\n-- copy / move / delete --");
        Path src  = tempDir.resolve("original.txt");
        Path copy = tempDir.resolve("copy.txt");
        Path dest = tempDir.resolve("moved.txt");

        Files.writeString(src, "important data", StandardCharsets.UTF_8);
        Files.copy(src, copy, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("  copy exists: " + Files.exists(copy));

        Files.move(copy, dest, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("  copy after move exists: " + Files.exists(copy));
        System.out.println("  dest after move exists:  " + Files.exists(dest));

        Files.deleteIfExists(src);
        Files.deleteIfExists(dest);
    }

    // -----------------------------------------------------------------------
    // File attributes
    // -----------------------------------------------------------------------
    public static void showAttributes(Path tempDir) throws IOException {
        System.out.println("\n-- File attributes --");
        Path file = tempDir.resolve("attr.txt");
        Files.writeString(file, "attribute demo", StandardCharsets.UTF_8);

        BasicFileAttributes attrs =
                Files.readAttributes(file, BasicFileAttributes.class);
        System.out.println("  size:         " + attrs.size() + " bytes");
        System.out.println("  isRegular:    " + attrs.isRegularFile());
        System.out.println("  isDirectory:  " + attrs.isDirectory());
        System.out.println("  createdTime:  " + attrs.creationTime().toInstant().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));

        Files.deleteIfExists(file);
    }

    // -----------------------------------------------------------------------
    // Directory walking with Files.walk and Files.walkFileTree
    // -----------------------------------------------------------------------
    public static void showDirectoryWalk(Path tempDir) throws IOException {
        System.out.println("\n-- Directory walk --");
        // Build a small tree.
        Path sub1 = tempDir.resolve("sub1");
        Path sub2 = tempDir.resolve("sub2");
        Files.createDirectories(sub1);
        Files.createDirectories(sub2);
        Files.writeString(sub1.resolve("a.txt"), "a");
        Files.writeString(sub1.resolve("b.txt"), "b");
        Files.writeString(sub2.resolve("c.txt"), "c");

        System.out.println("  All paths under tempDir:");
        try (var stream = Files.walk(tempDir)) {
            stream.filter(p -> !p.equals(tempDir))
                  .map(p -> "    " + tempDir.relativize(p) + (Files.isDirectory(p) ? "/" : ""))
                  .sorted()
                  .forEach(System.out::println);
        }

        System.out.println("  Only .txt files:");
        try (var stream = Files.walk(tempDir)) {
            stream.filter(p -> p.toString().endsWith(".txt"))
                  .map(p -> "    " + tempDir.relativize(p))
                  .sorted()
                  .forEach(System.out::println);
        }

        // Clean up the tree we just made.
        Files.walk(tempDir).sorted(java.util.Comparator.reverseOrder())
             .filter(p -> !p.equals(tempDir))
             .forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
    }
}
