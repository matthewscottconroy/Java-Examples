package com.algorithms.greedy.activity;

import java.util.List;

/**
 * Demonstrates activity scheduling on a conference room booking system.
 *
 * Meeting requests come in throughout the day. The scheduler picks the maximum
 * number of non-overlapping meetings for a single room, then shows how
 * multi-track scheduling handles multiple rooms.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Activity Scheduling — Conference Room Booking ===\n");

        List<ActivityScheduler.Activity> meetings = List.of(
            new ActivityScheduler.Activity("Sprint planning",  9, 10),
            new ActivityScheduler.Activity("1:1 with Alice",   9, 11),
            new ActivityScheduler.Activity("Code review",     10, 11),
            new ActivityScheduler.Activity("Design review",   11, 13),
            new ActivityScheduler.Activity("Lunch & Learn",   12, 13),
            new ActivityScheduler.Activity("Tech talk",       13, 15),
            new ActivityScheduler.Activity("Demo prep",       14, 15),
            new ActivityScheduler.Activity("All-hands",       15, 17),
            new ActivityScheduler.Activity("Retro",           16, 17),
            new ActivityScheduler.Activity("Team dinner",     17, 19)
        );

        System.out.println("All meeting requests:");
        System.out.printf("  %-25s  %5s  %5s%n", "Meeting", "Start", "End");
        System.out.println("  " + "-".repeat(38));
        meetings.forEach(m -> System.out.printf("  %-25s  %5d  %5d%n",
            m.name(), m.start(), m.end()));

        System.out.println("\n--- Single Room: Greedy Schedule ---");
        List<ActivityScheduler.Activity> single = ActivityScheduler.schedule(meetings);
        System.out.println("Maximum non-overlapping meetings: " + single.size());
        single.forEach(m -> System.out.printf("  [%2d–%2d] %s%n", m.start(), m.end(), m.name()));

        System.out.println("\n--- Two Rooms: Multi-Track Schedule ---");
        List<List<ActivityScheduler.Activity>> multiTrack =
            ActivityScheduler.scheduleMultiTrack(meetings, 2);
        for (int r = 0; r < multiTrack.size(); r++) {
            List<ActivityScheduler.Activity> track = multiTrack.get(r);
            if (!track.isEmpty()) {
                System.out.println("Room " + (r + 1) + ":");
                track.forEach(m -> System.out.printf("  [%2d–%2d] %s%n",
                    m.start(), m.end(), m.name()));
            }
        }

        int totalScheduled = multiTrack.stream().mapToInt(List::size).sum();
        System.out.println("\nTotal meetings scheduled across both rooms: " + totalScheduled);
    }
}
