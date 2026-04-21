package com.bugblaster.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Manages wave-based bug spawning.
 *
 * <p>Each wave has a fixed composition of bug types that are placed into a
 * spawn queue. The spawner releases one bug every {@code spawnInterval} ticks
 * so they trickle in rather than flooding the screen at once.
 *
 * <p>The wave is considered complete when the spawn queue is empty <em>and</em>
 * every bug in the main list is dead. The controller checks
 * {@link #isWaveComplete(List)} each tick to decide when to advance.
 *
 * <p>Wave compositions (bugs of each type per wave):
 * <pre>
 *   Wave  Ants  Roaches  Flies  Spiders  Beetles  Interval
 *     1    10      0       0       0        0       80
 *     2    14      4       0       0        0       70
 *     3    16      5       5       0        0       60
 *     4    18      6       6       3        0       52
 *     5    20      8       8       4        2       44
 *    6+   +4      +2      +2      +1       +1       40
 * </pre>
 */
public final class BugSpawner {

    private static final int GAME_W = 900;
    private static final int GAME_H = 620;

    /** Bug counts and spawn interval for each wave (index = wave-1, capped at last entry). */
    private static final int[][] WAVE_SPECS = {
        // ants, roaches, flies, spiders, beetles, interval
        { 10, 0, 0, 0, 0, 80 },
        { 14, 4, 0, 0, 0, 70 },
        { 16, 5, 5, 0, 0, 60 },
        { 18, 6, 6, 3, 0, 52 },
        { 20, 8, 8, 4, 2, 44 },
    };

    private final Deque<Bug> queue = new ArrayDeque<>();
    private int tick;
    private int spawnInterval;

    /**
     * Loads the spawn queue for the given wave number (1-indexed).
     * Call this whenever the wave advances.
     */
    public void loadWave(int waveNumber) {
        queue.clear();
        tick = 0;

        int idx = Math.min(waveNumber - 1, WAVE_SPECS.length - 1);
        int[] s = WAVE_SPECS[idx];

        // For waves beyond the table, scale up from the last entry
        int extra = Math.max(0, waveNumber - WAVE_SPECS.length);
        int ants     = s[0] + extra * 4;
        int roaches  = s[1] + extra * 2;
        int flies    = s[2] + extra * 2;
        int spiders  = s[3] + extra;
        int beetles  = s[4] + extra;
        spawnInterval = Math.max(30, s[5] - extra * 2);

        // Interleave bug types for variety
        int max = Math.max(ants, Math.max(roaches, Math.max(flies, Math.max(spiders, beetles))));
        for (int i = 0; i < max; i++) {
            if (i < ants)    queue.add(spawnAt(new AntBug(edgeX(), edgeY())));
            if (i < roaches) queue.add(spawnAt(new CockroachBug(edgeX(), edgeY())));
            if (i < flies)   queue.add(spawnAt(new FlyBug(edgeX(), edgeY())));
            if (i < spiders) queue.add(spawnAt(new SpiderBug(edgeX(), edgeY())));
            if (i < beetles) queue.add(spawnAt(new BeetleBug(edgeX(), edgeY())));
        }
    }

    /**
     * Advances the spawner by one tick, releasing queued bugs into {@code bugs}
     * at the configured interval.
     */
    public void update(List<Bug> bugs) {
        tick++;
        if (!queue.isEmpty() && tick % spawnInterval == 0) {
            bugs.add(queue.poll());
        }
    }

    /** Returns {@code true} when this wave is fully spawned and all bugs are dead. */
    public boolean isWaveComplete(List<Bug> bugs) {
        return queue.isEmpty() && bugs.stream().noneMatch(Bug::isAlive);
    }

    // ------------------------------------------------------------------ helpers

    /** Picks a random spawn position along any of the four edges. */
    private static double edgeX() {
        int edge = (int)(Math.random() * 4);
        return switch (edge) {
            case 0  -> 0;
            case 1  -> GAME_W;
            default -> Math.random() * GAME_W;
        };
    }

    private static double edgeY() {
        int edge = (int)(Math.random() * 4);
        return switch (edge) {
            case 2  -> 0;
            case 3  -> GAME_H;
            default -> Math.random() * GAME_H;
        };
    }

    /** Identity helper for readability — just returns the bug passed in. */
    private static Bug spawnAt(Bug bug) { return bug; }
}
