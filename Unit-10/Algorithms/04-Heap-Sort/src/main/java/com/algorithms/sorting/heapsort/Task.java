package com.algorithms.sorting.heapsort;

/**
 * A scheduled task with a priority (lower number = higher priority).
 */
public record Task(int priority, String name, String description) implements Comparable<Task> {

    @Override
    public int compareTo(Task other) {
        return Integer.compare(this.priority, other.priority);
    }

    @Override
    public String toString() {
        return String.format("[P%d] %-20s — %s", priority, name, description);
    }
}
