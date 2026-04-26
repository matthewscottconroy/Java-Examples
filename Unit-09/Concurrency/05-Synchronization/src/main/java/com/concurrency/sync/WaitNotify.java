package com.concurrency.sync;

/**
 * Classic producer-consumer using {@code wait()} and {@code notifyAll()}.
 *
 * <p>Rules for wait/notify (all three must hold):
 * <ol>
 *   <li>The calling thread must <strong>hold the monitor</strong> of the object
 *       on which it calls wait/notify.</li>
 *   <li>Always call {@code wait()} in a <strong>loop</strong> that re-checks the
 *       condition — spurious wakeups can occur on any JVM.</li>
 *   <li>Prefer {@code notifyAll()} over {@code notify()} unless you can prove
 *       only one waiter can act; {@code notify()} may wake the wrong thread.</li>
 * </ol>
 *
 * <p>For new code, prefer {@link java.util.concurrent.locks.Condition} (example 06)
 * or a {@link java.util.concurrent.BlockingQueue} (example 07), both of which
 * encapsulate this pattern safely.
 */
public class WaitNotify {

    static class BoundedBuffer<T> {
        private final Object[] buffer;
        private int head = 0, tail = 0, count = 0;

        BoundedBuffer(int capacity) { buffer = new Object[capacity]; }

        public synchronized void put(T item) throws InterruptedException {
            // Loop — not if — to guard against spurious wakeups.
            while (count == buffer.length) {
                System.out.println("  buffer full, producer waiting…");
                wait();         // releases the lock; re-acquires before returning
            }
            buffer[tail] = item;
            tail = (tail + 1) % buffer.length;
            count++;
            System.out.println("  put " + item + "  (size=" + count + ")");
            notifyAll();        // wake any waiting consumers
        }

        @SuppressWarnings("unchecked")
        public synchronized T take() throws InterruptedException {
            while (count == 0) {
                System.out.println("  buffer empty, consumer waiting…");
                wait();
            }
            T item = (T) buffer[head];
            head = (head + 1) % buffer.length;
            count--;
            System.out.println("  took " + item + " (size=" + count + ")");
            notifyAll();        // wake any waiting producers
            return item;
        }
    }

    public static void demonstrate() throws InterruptedException {
        BoundedBuffer<Integer> buf = new BoundedBuffer<>(2);

        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    buf.put(i);
                    Thread.sleep(20);
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "producer");

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    buf.take();
                    Thread.sleep(50);   // consumer slower than producer → forces waits
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "consumer");

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
    }
}
