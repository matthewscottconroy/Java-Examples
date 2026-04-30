package com.algorithms.greedy.activity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActivitySchedulerTest {

    @Test
    @DisplayName("schedule selects maximum non-overlapping activities")
    void scheduleMaximum() {
        List<ActivityScheduler.Activity> acts = List.of(
            new ActivityScheduler.Activity("A", 1, 4),
            new ActivityScheduler.Activity("B", 3, 5),
            new ActivityScheduler.Activity("C", 0, 6),
            new ActivityScheduler.Activity("D", 5, 7),
            new ActivityScheduler.Activity("E", 3, 9),
            new ActivityScheduler.Activity("F", 5, 9),
            new ActivityScheduler.Activity("G", 6, 10),
            new ActivityScheduler.Activity("H", 8, 11),
            new ActivityScheduler.Activity("I", 8, 12),
            new ActivityScheduler.Activity("J", 2, 14),
            new ActivityScheduler.Activity("K", 12, 16)
        );
        List<ActivityScheduler.Activity> result = ActivityScheduler.schedule(acts);
        // Classic result: A, D, G/H, K — 4 activities max
        assertEquals(4, result.size());
    }

    @Test
    @DisplayName("scheduled activities do not overlap")
    void noOverlaps() {
        List<ActivityScheduler.Activity> acts = List.of(
            new ActivityScheduler.Activity("A", 0, 3),
            new ActivityScheduler.Activity("B", 2, 5),
            new ActivityScheduler.Activity("C", 4, 7),
            new ActivityScheduler.Activity("D", 6, 9)
        );
        List<ActivityScheduler.Activity> result = ActivityScheduler.schedule(acts);
        for (int i = 1; i < result.size(); i++) {
            assertFalse(result.get(i - 1).overlapsWith(result.get(i)),
                "Activities " + (i-1) + " and " + i + " overlap");
        }
    }

    @Test
    @DisplayName("empty input returns empty list")
    void emptyInput() {
        assertTrue(ActivityScheduler.schedule(List.of()).isEmpty());
    }

    @Test
    @DisplayName("single activity is always selected")
    void singleActivity() {
        List<ActivityScheduler.Activity> acts = List.of(
            new ActivityScheduler.Activity("solo", 5, 10));
        assertEquals(1, ActivityScheduler.schedule(acts).size());
    }

    @Test
    @DisplayName("non-overlapping chain: all activities selected")
    void allNonOverlapping() {
        List<ActivityScheduler.Activity> acts = List.of(
            new ActivityScheduler.Activity("A", 0, 2),
            new ActivityScheduler.Activity("B", 2, 4),
            new ActivityScheduler.Activity("C", 4, 6)
        );
        assertEquals(3, ActivityScheduler.schedule(acts).size());
    }

    @Test
    @DisplayName("all overlapping: only one activity selected")
    void allOverlapping() {
        List<ActivityScheduler.Activity> acts = List.of(
            new ActivityScheduler.Activity("A", 0, 10),
            new ActivityScheduler.Activity("B", 1, 9),
            new ActivityScheduler.Activity("C", 2, 8)
        );
        assertEquals(1, ActivityScheduler.schedule(acts).size());
    }

    @Test
    @DisplayName("multi-track: two rooms handle more activities than one")
    void multiTrackMoreThanSingle() {
        List<ActivityScheduler.Activity> acts = List.of(
            new ActivityScheduler.Activity("A", 0, 4),
            new ActivityScheduler.Activity("B", 1, 5),
            new ActivityScheduler.Activity("C", 4, 8),
            new ActivityScheduler.Activity("D", 5, 9)
        );
        int single = ActivityScheduler.schedule(acts).size();
        int dual = ActivityScheduler.scheduleMultiTrack(acts, 2).stream()
            .mapToInt(List::size).sum();
        assertTrue(dual >= single);
    }

    @Test
    @DisplayName("multi-track: no track has overlapping activities")
    void multiTrackNoOverlaps() {
        List<ActivityScheduler.Activity> acts = List.of(
            new ActivityScheduler.Activity("A", 0, 3),
            new ActivityScheduler.Activity("B", 1, 4),
            new ActivityScheduler.Activity("C", 3, 6),
            new ActivityScheduler.Activity("D", 4, 7),
            new ActivityScheduler.Activity("E", 6, 9)
        );
        List<List<ActivityScheduler.Activity>> tracks =
            ActivityScheduler.scheduleMultiTrack(acts, 2);
        for (List<ActivityScheduler.Activity> track : tracks) {
            for (int i = 1; i < track.size(); i++) {
                assertFalse(track.get(i - 1).overlapsWith(track.get(i)));
            }
        }
    }
}
