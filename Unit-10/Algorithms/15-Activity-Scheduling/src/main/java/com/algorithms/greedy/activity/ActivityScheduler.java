package com.algorithms.greedy.activity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Activity Selection Problem: given a set of activities with start and end times,
 * find the maximum number of non-overlapping activities that can be scheduled.
 *
 * <p>The greedy algorithm selects the activity that finishes earliest first.
 * This is optimal: an earliest-finish choice never blocks more future activities
 * than any alternative choice would.
 *
 * <p>Time: O(n log n) to sort, O(n) for the selection pass.
 */
public final class ActivityScheduler {

    private ActivityScheduler() {}

    public record Activity(String name, int start, int end) {
        public boolean overlapsWith(Activity other) {
            return this.start < other.end && other.start < this.end;
        }
    }

    /**
     * Returns the maximum set of non-overlapping activities, sorted by finish time.
     * Activities that start exactly when another ends are considered non-overlapping.
     */
    public static List<Activity> schedule(List<Activity> activities) {
        List<Activity> sorted = activities.stream()
            .sorted(Comparator.comparingInt(Activity::end))
            .toList();

        List<Activity> selected = new ArrayList<>();
        int lastEnd = Integer.MIN_VALUE;

        for (Activity a : sorted) {
            if (a.start() >= lastEnd) {
                selected.add(a);
                lastEnd = a.end();
            }
        }
        return selected;
    }

    /**
     * Returns the maximum number of activities that can be scheduled
     * across {@code rooms} parallel rooms/tracks.
     *
     * <p>Uses a greedy multi-track assignment: for each activity (sorted by start),
     * assign it to any room whose last activity has already ended; otherwise open
     * a new room (if capacity allows).
     */
    public static List<List<Activity>> scheduleMultiTrack(List<Activity> activities, int rooms) {
        List<Activity> sorted = activities.stream()
            .sorted(Comparator.comparingInt(Activity::start))
            .toList();

        List<List<Activity>> tracks = new ArrayList<>();
        int[] trackEnd = new int[rooms];
        for (int i = 0; i < rooms; i++) {
            tracks.add(new ArrayList<>());
            trackEnd[i] = Integer.MIN_VALUE;
        }

        for (Activity a : sorted) {
            int bestTrack = -1;
            for (int t = 0; t < rooms; t++) {
                if (trackEnd[t] <= a.start()) {
                    if (bestTrack == -1 || trackEnd[t] > trackEnd[bestTrack]) bestTrack = t;
                }
            }
            if (bestTrack != -1) {
                tracks.get(bestTrack).add(a);
                trackEnd[bestTrack] = a.end();
            }
        }
        return tracks;
    }
}
