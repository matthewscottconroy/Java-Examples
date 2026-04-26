package com.concurrency.locks;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

/**
 * {@link ReentrantReadWriteLock} and {@link StampedLock}.
 *
 * <p><strong>ReadWriteLock invariant:</strong>
 * <ul>
 *   <li>Multiple threads may hold the read lock simultaneously.</li>
 *   <li>Only one thread may hold the write lock, and only when no readers hold it.</li>
 * </ul>
 * Best for read-heavy workloads where reads vastly outnumber writes.
 *
 * <p><strong>StampedLock</strong> adds <em>optimistic reads</em>: try to read
 * without acquiring the lock, then validate.  If validation fails (a write
 * happened), fall back to a proper read lock.  Higher throughput for very
 * read-heavy scenarios.
 */
public class ReadWriteDemo {

    // -----------------------------------------------------------------------
    // ReentrantReadWriteLock: concurrent reads, exclusive writes
    // -----------------------------------------------------------------------
    static class Cache {
        private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        private final ReentrantReadWriteLock.ReadLock  readLock  = rwLock.readLock();
        private final ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();

        private String data = "initial";

        public String read() {
            readLock.lock();
            try {
                return data;
            } finally { readLock.unlock(); }
        }

        public void write(String newData) {
            writeLock.lock();
            try {
                System.out.println("  write: updating to \"" + newData + "\"");
                data = newData;
            } finally { writeLock.unlock(); }
        }
    }

    public static void showReadWriteLock() throws InterruptedException {
        System.out.println("-- ReentrantReadWriteLock --");
        Cache cache = new Cache();

        // Launch 3 concurrent readers and 1 writer.
        Thread w = new Thread(() -> {
            try { Thread.sleep(30); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            cache.write("updated");
        }, "writer");

        Thread[] readers = new Thread[3];
        for (int i = 0; i < 3; i++) {
            final int id = i;
            readers[i] = new Thread(() -> {
                for (int r = 0; r < 3; r++) {
                    System.out.println("  reader-" + id + " read: \"" + cache.read() + "\"");
                    try { Thread.sleep(20); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
            }, "reader-" + i);
        }

        w.start();
        for (Thread r : readers) r.start();
        w.join();
        for (Thread r : readers) r.join();
    }

    // -----------------------------------------------------------------------
    // StampedLock: optimistic read avoids acquiring a lock at all
    // -----------------------------------------------------------------------
    static class Point {
        private final StampedLock sl = new StampedLock();
        private double x, y;

        public void move(double dx, double dy) {
            long stamp = sl.writeLock();
            try { x += dx; y += dy; }
            finally { sl.unlockWrite(stamp); }
        }

        public double distanceFromOrigin() {
            // 1. Try optimistic read — no lock acquired.
            long stamp = sl.tryOptimisticRead();
            double cx = x, cy = y;

            // 2. Validate: if a write happened since tryOptimisticRead(), stamp is invalid.
            if (!sl.validate(stamp)) {
                // 3. Fall back to a full read lock.
                stamp = sl.readLock();
                try { cx = x; cy = y; }
                finally { sl.unlockRead(stamp); }
                System.out.println("  optimistic read failed — fell back to read lock");
            }
            return Math.hypot(cx, cy);
        }
    }

    public static void showStampedLock() throws InterruptedException {
        System.out.println("\n-- StampedLock (optimistic read) --");
        Point p = new Point();
        p.move(3, 4);
        System.out.println("  distance from origin (no contention): " + p.distanceFromOrigin());

        // Introduce a writer mid-read to trigger fallback.
        Thread writer = new Thread(() -> {
            try { Thread.sleep(5); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            p.move(1, 1);
        });
        writer.start();
        System.out.println("  distance (with concurrent writer): " + p.distanceFromOrigin());
        writer.join();
    }
}
