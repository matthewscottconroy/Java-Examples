package com.algorithms.search.binary;

/**
 * A software release with a semantic version number and a flag indicating
 * whether a known regression was introduced in that version.
 */
public record Release(int major, int minor, int patch, boolean broken) implements Comparable<Release> {

    @Override
    public int compareTo(Release other) {
        int c = Integer.compare(this.major, other.major);
        if (c != 0) return c;
        c = Integer.compare(this.minor, other.minor);
        if (c != 0) return c;
        return Integer.compare(this.patch, other.patch);
    }

    @Override
    public String toString() {
        return String.format("v%d.%d.%d%s", major, minor, patch, broken ? " [BROKEN]" : "");
    }
}
