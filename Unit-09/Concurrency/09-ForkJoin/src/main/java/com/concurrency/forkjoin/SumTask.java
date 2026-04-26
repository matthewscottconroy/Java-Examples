package com.concurrency.forkjoin;

import java.util.concurrent.RecursiveTask;

/**
 * Parallel sum using {@link RecursiveTask}&lt;Long&gt;.
 *
 * <p>The Fork/Join framework uses a <em>work-stealing</em> algorithm:
 * idle threads steal tasks from the back of busy threads' deques.
 * This keeps all cores busy with minimal coordination overhead.
 *
 * <p>Divide-and-conquer recipe:
 * <ol>
 *   <li>If the problem is small enough (≤ threshold), solve it directly.</li>
 *   <li>Otherwise, split in two: {@code fork()} the left half (queues it),
 *       {@code compute()} the right half (runs it on this thread),
 *       then {@code join()} the left half (waits for result).</li>
 * </ol>
 *
 * <p>Always {@code fork} first and {@code compute} second, not the reverse —
 * this lets this thread do useful work while the forked subtask runs.
 */
public class SumTask extends RecursiveTask<Long> {

    private static final int THRESHOLD = 1_000;

    private final long[] array;
    private final int    from, to;

    public SumTask(long[] array) { this(array, 0, array.length); }

    private SumTask(long[] array, int from, int to) {
        this.array = array;
        this.from  = from;
        this.to    = to;
    }

    @Override
    protected Long compute() {
        int length = to - from;

        if (length <= THRESHOLD) {
            // Base case: sequential sum.
            long sum = 0;
            for (int i = from; i < to; i++) sum += array[i];
            return sum;
        }

        // Recursive case: split in half.
        int mid = from + length / 2;
        SumTask left  = new SumTask(array, from, mid);
        SumTask right = new SumTask(array, mid, to);

        left.fork();                    // queue left subtask for another thread
        long rightResult = right.compute();  // compute right on this thread
        long leftResult  = left.join();      // wait for left

        return leftResult + rightResult;
    }
}
