package com.functional.recursion;

import java.util.List;

/**
 * A node in a virtual file system tree.
 *
 * <p>A leaf node has no children and a positive size. A directory node
 * has children and a size of 0 (its size is the recursive sum of children).
 *
 * @param name     file or directory name
 * @param sizeBytes size in bytes (0 for directories)
 * @param children child nodes (empty for files)
 */
public record FileNode(String name, long sizeBytes, List<FileNode> children) {

    /** True if this node is a directory (has children). */
    public boolean isDirectory() { return !children.isEmpty(); }

    /** Convenience factory for a file leaf. */
    public static FileNode file(String name, long sizeBytes) {
        return new FileNode(name, sizeBytes, List.of());
    }

    /** Convenience factory for a directory. */
    public static FileNode dir(String name, FileNode... children) {
        return new FileNode(name, 0, List.of(children));
    }
}
