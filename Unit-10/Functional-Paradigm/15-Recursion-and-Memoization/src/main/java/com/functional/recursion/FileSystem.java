package com.functional.recursion;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Recursive algorithms on the virtual file-system tree.
 */
public final class FileSystem {

    private FileSystem() {}

    /**
     * Recursively compute the total size of a node in bytes.
     *
     * <p>Base case: a file returns its own size.
     * Recursive case: a directory returns the sum of its children's total sizes.
     */
    public static long totalSize(FileNode node) {
        if (!node.isDirectory()) return node.sizeBytes();
        return node.children().stream()
                .mapToLong(FileSystem::totalSize)
                .sum();
    }

    /**
     * Recursively collect all file paths under a node.
     *
     * @param node   the root of the subtree
     * @param prefix path accumulated so far
     * @return list of absolute paths of all leaf files
     */
    public static List<String> allFilePaths(FileNode node, String prefix) {
        String path = prefix + "/" + node.name();
        if (!node.isDirectory()) return List.of(path);

        List<String> paths = new ArrayList<>();
        for (FileNode child : node.children()) {
            paths.addAll(allFilePaths(child, path));
        }
        return paths;
    }

    /**
     * Recursively find all files larger than {@code thresholdBytes}.
     */
    public static List<FileNode> largerThan(FileNode node, long thresholdBytes) {
        List<FileNode> results = new ArrayList<>();
        if (!node.isDirectory()) {
            if (node.sizeBytes() > thresholdBytes) results.add(node);
        } else {
            for (FileNode child : node.children()) {
                results.addAll(largerThan(child, thresholdBytes));
            }
        }
        return results;
    }

    /**
     * Print the tree structure with indentation.
     */
    public static void print(FileNode node, int depth) {
        String indent = "  ".repeat(depth);
        String size   = node.isDirectory()
                ? String.format("(%,d bytes total)", totalSize(node))
                : String.format("(%,d bytes)", node.sizeBytes());
        System.out.printf("%s%s  %s%n", indent, node.name(), size);
        for (FileNode child : node.children()) print(child, depth + 1);
    }
}
