package com.concurrency.atomic;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.*;

/**
 * Lock-free atomic variables via Compare-And-Swap (CAS).
 *
 * <p>CAS: "If the current value equals the expected value, replace it
 * with the new value; otherwise report failure."  The hardware guarantees
 * this is atomic.  Unlike {@code synchronized}, a failed CAS just retries
 * rather than blocking — no thread ever waits on another.
 *
 * <p>Use cases:
 * <ul>
 *   <li>{@link AtomicInteger}  — counters, sequence numbers</li>
 *   <li>{@link AtomicLong}     — same, for longs</li>
 *   <li>{@link AtomicBoolean}  — flags</li>
 *   <li>{@link AtomicReference} — single-object CAS (e.g. stack head)</li>
 *   <li>{@link LongAdder}      — high-contention counters; much faster than AtomicLong under load</li>
 * </ul>
 */
public class AtomicDemo {

    public static void showAtomicInteger() throws InterruptedException {
        System.out.println("-- AtomicInteger: lock-free increment --");
        AtomicInteger counter = new AtomicInteger(0);
        final int THREADS = 8, ITERS = 10_000;
        CountDownLatch latch = new CountDownLatch(THREADS);

        for (int i = 0; i < THREADS; i++) {
            new Thread(() -> {
                for (int j = 0; j < ITERS; j++) {
                    counter.incrementAndGet();  // atomic: no lock, no lost update
                }
                latch.countDown();
            }).start();
        }
        latch.await();
        System.out.println("  expected " + (THREADS * ITERS) + "  actual " + counter.get());

        System.out.println("\n-- compareAndSet (CAS) --");
        AtomicInteger val = new AtomicInteger(10);
        boolean updated = val.compareAndSet(10, 20);   // expect 10 → set 20
        System.out.println("  CAS(10→20) succeeded: " + updated + "  value=" + val.get());
        updated = val.compareAndSet(10, 30);           // expect 10 — but value is now 20 → fail
        System.out.println("  CAS(10→30) succeeded: " + updated + "  value=" + val.get());

        System.out.println("\n-- getAndUpdate / updateAndGet --");
        AtomicInteger seq = new AtomicInteger(0);
        System.out.println("  getAndIncrement: " + seq.getAndIncrement());  // 0
        System.out.println("  getAndAdd(5):    " + seq.getAndAdd(5));       // 1
        System.out.println("  updateAndGet(*2):" + seq.updateAndGet(n -> n * 2)); // 12
    }

    public static void showLongAdder() throws InterruptedException {
        System.out.println("\n-- LongAdder: better throughput under high contention --");
        // LongAdder maintains a set of cells to distribute writes; sum() combines them.
        // Under contention it is significantly faster than AtomicLong.incrementAndGet().
        LongAdder adder = new LongAdder();
        final int THREADS = 8, ITERS = 100_000;
        CountDownLatch latch = new CountDownLatch(THREADS);

        for (int i = 0; i < THREADS; i++) {
            new Thread(() -> {
                for (int j = 0; j < ITERS; j++) adder.increment();
                latch.countDown();
            }).start();
        }
        latch.await();
        System.out.println("  LongAdder.sum() = " + adder.sum()
                + "  expected = " + (long) THREADS * ITERS);
    }

    public static void showAtomicReference() {
        System.out.println("\n-- AtomicReference: CAS on an object reference --");
        record Config(String host, int port) {}

        AtomicReference<Config> configRef =
                new AtomicReference<>(new Config("localhost", 8080));

        Config current = configRef.get();
        Config updated = new Config("prod.example.com", 443);
        boolean swapped = configRef.compareAndSet(current, updated);
        System.out.println("  swap succeeded: " + swapped);
        System.out.println("  new config: " + configRef.get());

        // Second swap with stale expected — fails because config already changed.
        boolean staleCas = configRef.compareAndSet(current, new Config("other", 80));
        System.out.println("  stale CAS: " + staleCas);
    }
}
