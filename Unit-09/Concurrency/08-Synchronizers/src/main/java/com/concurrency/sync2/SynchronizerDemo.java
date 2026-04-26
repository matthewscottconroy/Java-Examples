package com.concurrency.sync2;

import java.util.concurrent.*;

/**
 * The five high-level thread coordination utilities.
 *
 * <ul>
 *   <li>{@link CountDownLatch}  — one-shot countdown; cannot be reset</li>
 *   <li>{@link CyclicBarrier}   — reusable barrier with optional action when all arrive</li>
 *   <li>{@link Semaphore}       — permit-based access control (generalised lock)</li>
 *   <li>{@link Phaser}          — flexible, dynamic barrier; threads can register/deregister</li>
 *   <li>{@link Exchanger}       — synchronous pairwise handoff between two threads</li>
 * </ul>
 */
public class SynchronizerDemo {

    // -----------------------------------------------------------------------
    // CountDownLatch — two idioms
    // -----------------------------------------------------------------------
    public static void showCountDownLatch() throws InterruptedException {
        System.out.println("-- CountDownLatch: all-ready-then-go race start --");
        final int RUNNERS = 4;
        CountDownLatch ready = new CountDownLatch(RUNNERS);  // runners signal ready
        CountDownLatch start = new CountDownLatch(1);         // starter fires the gun
        CountDownLatch done  = new CountDownLatch(RUNNERS);  // main waits for all finishes

        for (int i = 1; i <= RUNNERS; i++) {
            final int id = i;
            new Thread(() -> {
                System.out.println("  runner-" + id + " ready");
                ready.countDown();                  // tell starter I'm ready
                try {
                    start.await();                  // wait at the starting line
                    Thread.sleep((long)(Math.random() * 60) + 20);
                    System.out.println("  runner-" + id + " finished");
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                done.countDown();
            }).start();
        }

        ready.await();                          // wait until all runners are ready
        System.out.println("  ALL RUNNERS READY — GO!");
        start.countDown();                      // fire the starting gun (releases all at once)
        done.await();
        System.out.println("  race complete");
    }

    // -----------------------------------------------------------------------
    // CyclicBarrier — reusable; optional action when the barrier is reached
    // -----------------------------------------------------------------------
    public static void showCyclicBarrier() throws Exception {
        System.out.println("\n-- CyclicBarrier: phased parallel computation --");
        final int WORKERS = 3;
        int[] phase = {1};

        CyclicBarrier barrier = new CyclicBarrier(WORKERS, () ->
                System.out.println("  *** barrier action: all workers finished phase " + phase[0]++ + " ***"));

        for (int i = 1; i <= WORKERS; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    for (int round = 1; round <= 2; round++) {
                        Thread.sleep((long)(Math.random() * 50) + 10);
                        System.out.println("  worker-" + id + " done with phase, waiting at barrier");
                        barrier.await();    // blocks until all WORKERS have called await()
                    }
                } catch (Exception e) { Thread.currentThread().interrupt(); }
            }).start();
        }
        Thread.sleep(500);  // let all phases complete
    }

    // -----------------------------------------------------------------------
    // Semaphore — limit concurrent access to a resource
    // -----------------------------------------------------------------------
    public static void showSemaphore() throws InterruptedException {
        System.out.println("\n-- Semaphore: max 2 concurrent database connections --");
        Semaphore sem = new Semaphore(2);   // 2 permits = 2 simultaneous connections allowed

        for (int i = 1; i <= 5; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    sem.acquire();  // blocks if no permits available
                    System.out.println("  thread-" + id + " acquired connection (permits left: " + sem.availablePermits() + ")");
                    Thread.sleep(60);
                    System.out.println("  thread-" + id + " releasing connection");
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                finally { sem.release(); }
            }).start();
            Thread.sleep(10);
        }
        Thread.sleep(400);
    }

    // -----------------------------------------------------------------------
    // Exchanger — synchronous pairwise handoff
    // -----------------------------------------------------------------------
    public static void showExchanger() throws InterruptedException {
        System.out.println("\n-- Exchanger: producer/consumer swap buffers --");
        Exchanger<String> exchanger = new Exchanger<>();

        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 3; i++) {
                    String produced = "batch-" + i;
                    System.out.println("  producer: sending " + produced);
                    // exchange() blocks until the partner also calls exchange(),
                    // then both receive what the other sent.
                    String received = exchanger.exchange(produced);
                    System.out.println("  producer: got receipt: " + received);
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "producer");

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= 3; i++) {
                    Thread.sleep(30);
                    String data = exchanger.exchange("ack-" + i);
                    System.out.println("  consumer: received " + data);
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "consumer");

        producer.start(); consumer.start();
        producer.join(); consumer.join();
    }
}
