package com.concurrency.locks;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link Condition} — the explicit-lock replacement for {@code wait/notifyAll}.
 *
 * <p>Advantages over {@code Object.wait/notify}:
 * <ul>
 *   <li>A single {@link ReentrantLock} can have <em>multiple</em> Conditions,
 *       so producers and consumers have separate wait sets.  Only the relevant
 *       set is notified — no wasted wakeups.</li>
 *   <li>{@code await(time, unit)} — timed wait without try/catch arithmetic.</li>
 *   <li>{@code awaitUninterruptibly()} — wait that cannot be interrupted.</li>
 * </ul>
 *
 * <p>Rule: always call {@code await()} inside a loop that re-checks the condition.
 */
public class ConditionDemo {

    static class BoundedBuffer<T> {
        private final Object[] buffer;
        private int head = 0, tail = 0, count = 0;

        private final ReentrantLock lock    = new ReentrantLock();
        private final Condition     notFull = lock.newCondition();  // signalled when space opens
        private final Condition     notEmpty= lock.newCondition();  // signalled when item arrives

        BoundedBuffer(int capacity) { buffer = new Object[capacity]; }

        public void put(T item) throws InterruptedException {
            lock.lock();
            try {
                while (count == buffer.length) {
                    System.out.println("  producer waiting (buffer full)…");
                    notFull.await();     // releases lock; producer waits on THIS condition only
                }
                buffer[tail] = item;
                tail = (tail + 1) % buffer.length;
                count++;
                System.out.println("  put " + item + " (size=" + count + ")");
                notEmpty.signal();       // wake exactly one waiting consumer
            } finally { lock.unlock(); }
        }

        @SuppressWarnings("unchecked")
        public T take() throws InterruptedException {
            lock.lock();
            try {
                while (count == 0) {
                    System.out.println("  consumer waiting (buffer empty)…");
                    notEmpty.await();
                }
                T item = (T) buffer[head];
                head = (head + 1) % buffer.length;
                count--;
                System.out.println("  took " + item + " (size=" + count + ")");
                notFull.signal();        // wake exactly one waiting producer
                return item;
            } finally { lock.unlock(); }
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
                    Thread.sleep(55);
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "consumer");

        producer.start(); consumer.start();
        producer.join();  consumer.join();
    }
}
