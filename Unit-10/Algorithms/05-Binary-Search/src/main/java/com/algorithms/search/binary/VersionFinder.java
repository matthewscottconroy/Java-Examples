package com.algorithms.search.binary;

import java.util.List;

/**
 * Finds the first broken release in a sorted release history.
 *
 * This is the classic "find first bad version" interview problem: given a
 * sorted list of releases where all releases from some version onward are
 * broken, binary-search for the boundary.
 */
public final class VersionFinder {

    private VersionFinder() {}

    /**
     * Returns the first release that is broken, or null if none are broken.
     * Assumes releases are sorted from oldest to newest.
     */
    public static Release firstBroken(List<Release> releases) {
        int lo = 0, hi = releases.size() - 1;
        Release result = null;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (releases.get(mid).broken()) {
                result = releases.get(mid);
                hi = mid - 1;  // maybe there's an earlier broken one
            } else {
                lo = mid + 1;
            }
        }
        return result;
    }
}
