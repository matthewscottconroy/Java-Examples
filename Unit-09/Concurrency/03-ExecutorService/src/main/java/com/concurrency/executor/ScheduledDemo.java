package com.concurrency.executor;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

/**
 * Demonstrates {@link ScheduledExecutorService}.
 *
 * <ul>
 *   <li>{@code schedule()}              — run once after a delay</li>
 *   <li>{@code scheduleAtFixedRate()}   — run every N ms from the start of the previous execution</li>
 *   <li>{@code scheduleWithFixedDelay()} — wait N ms after the previous execution finishes</li>
 * </ul>
 *
 * <p>Prefer this over {@code java.util.Timer}: Timer uses a single thread and
 * a late task delays all subsequent tasks; a ScheduledExecutorService with
 * multiple threads avoids that, and uncaught exceptions don't kill the scheduler.
 */
public class ScheduledDemo {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    static String now() { return LocalTime.now().format(FMT); }

    public static void demonstrate() throws Exception {
        ScheduledExecutorService sched = Executors.newScheduledThreadPool(2);

        System.out.println("-- schedule(): one-shot delay --");
        ScheduledFuture<String> onceResult = sched.schedule(
                () -> { System.out.println("  one-shot at " + now()); return "done"; },
                80, TimeUnit.MILLISECONDS);
        System.out.println("  submitted at " + now());
        System.out.println("  result: " + onceResult.get());

        System.out.println("\n-- scheduleAtFixedRate(): every 50 ms --");
        int[] rateCount = {0};
        ScheduledFuture<?> rate = sched.scheduleAtFixedRate(
                () -> System.out.println("  fixedRate tick at " + now()),
                0, 50, TimeUnit.MILLISECONDS);
        Thread.sleep(180);
        rate.cancel(false);

        System.out.println("\n-- scheduleWithFixedDelay(): 30 ms after each finish --");
        int[] delayCount = {0};
        ScheduledFuture<?> delay = sched.scheduleWithFixedDelay(() -> {
            System.out.println("  fixedDelay start " + now());
            try { Thread.sleep(20); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            System.out.println("  fixedDelay end   " + now());
        }, 0, 30, TimeUnit.MILLISECONDS);
        Thread.sleep(200);
        delay.cancel(false);

        sched.shutdown();
        sched.awaitTermination(1, TimeUnit.SECONDS);
    }
}
