package com.io.nio;

import java.nio.file.*;

public class Main {

    public static void main(String[] args) throws Exception {
        Path tempDir = Files.createTempDirectory("io-nio-");
        try {
            System.out.println("=== try-with-resources and AutoCloseable ===");
            ResourceDemo.showMultipleResources();
            ResourceDemo.showCustomAutoCloseable();
            ResourceDemo.showSuppressedException();

            System.out.println("\n=== Files utility (NIO.2) ===");
            FilesDemo.showReadWrite(tempDir);
            FilesDemo.showCopyMoveDelete(tempDir);
            FilesDemo.showAttributes(tempDir);
            FilesDemo.showDirectoryWalk(tempDir);

            System.out.println("\n=== Path API ===");
            Path p = Path.of("/home/user/projects/app/src/Main.java");
            System.out.println("  path:         " + p);
            System.out.println("  parent:       " + p.getParent());
            System.out.println("  fileName:     " + p.getFileName());
            System.out.println("  root:         " + p.getRoot());
            System.out.println("  nameCount:    " + p.getNameCount());
            System.out.println("  getName(2):   " + p.getName(2));
            System.out.println("  subpath(2,4): " + p.subpath(2, 4));

            Path base = Path.of("/home/user/projects");
            System.out.println("  relativize:   " + base.relativize(p));
            System.out.println("  resolve:      " + base.resolve("other/Util.java"));
            System.out.println("  normalize:    " + Path.of("/a/b/../c/./d").normalize());
        } finally {
            Files.deleteIfExists(tempDir);
        }
    }
}
