package com.concurrency.atomic;

import java.util.concurrent.*;

/**
 * Thread-safe collection types from {@code java.util.concurrent}.
 *
 * <p><strong>ConcurrentHashMap</strong> — fully concurrent map (Java 8+).
 * Individual operations are atomic via fine-grained CAS + bin-level synchronisation.
 * {@code computeIfAbsent}, {@code merge}, {@code compute} let you do
 * read-modify-write atomically without external locks.
 *
 * <p><strong>CopyOnWriteArrayList</strong> — every mutation creates a new
 * backing array; readers see a snapshot and never block.  Best for tiny lists
 * that are read very frequently and written rarely (e.g. listener lists).
 */
public class ConcurrentCollections {

    public static void showConcurrentHashMap() throws InterruptedException {
        System.out.println("-- ConcurrentHashMap --");
        ConcurrentHashMap<String, Integer> wordCount = new ConcurrentHashMap<>();

        // merge: atomic "compute only if key present, otherwise set default"
        String[] words = {"apple", "banana", "apple", "cherry", "banana", "apple"};
        for (String w : words) {
            wordCount.merge(w, 1, Integer::sum);   // atomic: no explicit lock needed
        }
        System.out.println("  word counts: " + wordCount);

        // computeIfAbsent: atomic check-then-insert
        wordCount.computeIfAbsent("durian", k -> 0);
        System.out.println("  after computeIfAbsent(durian): " + wordCount);

        // Concurrent modification during iteration is safe (iterates a snapshot).
        System.out.println("  iterating while another thread modifies:");
        Thread modifier = new Thread(() -> wordCount.put("elderberry", 1));
        modifier.start();
        wordCount.forEach((k, v) -> System.out.println("    " + k + "=" + v));
        modifier.join();
    }

    public static void showCopyOnWrite() throws InterruptedException {
        System.out.println("\n-- CopyOnWriteArrayList --");
        CopyOnWriteArrayList<String> listeners = new CopyOnWriteArrayList<>();
        listeners.add("listener-A");
        listeners.add("listener-B");

        // Reader iterates over a stable snapshot — unaffected by concurrent add/remove.
        Thread reader = new Thread(() -> {
            for (String l : listeners) {        // snapshot taken at iterator creation
                System.out.println("  reader sees: " + l);
                try { Thread.sleep(30); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        });
        Thread writer = new Thread(() -> {
            try { Thread.sleep(15); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            listeners.add("listener-C");        // creates a new backing array
            System.out.println("  writer added listener-C");
        });

        reader.start(); writer.start();
        reader.join(); writer.join();
        System.out.println("  final list: " + listeners);
    }

    public static void showBlockingQueue() throws InterruptedException {
        System.out.println("\n-- ArrayBlockingQueue: bounded producer-consumer --");
        // Bounded queue provides backpressure: put() blocks when full, take() blocks when empty.
        ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<>(3);

        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 7; i++) {
                    System.out.println("  producing " + i + " (queue size=" + queue.size() + ")");
                    queue.put(i);       // blocks if queue is full
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "producer");

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < 7; i++) {
                    int v = queue.take();   // blocks if queue is empty
                    System.out.println("  consumed " + v);
                    Thread.sleep(40);       // slower than producer → queue fills up
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "consumer");

        producer.start(); consumer.start();
        producer.join(); consumer.join();
    }
}
