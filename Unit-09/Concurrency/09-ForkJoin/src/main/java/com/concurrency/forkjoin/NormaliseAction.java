package com.concurrency.forkjoin;

import java.util.concurrent.RecursiveAction;

/**
 * Parallel in-place normalisation using {@link RecursiveAction} (no return value).
 *
 * <p>Divides an array of doubles and scales each element by a factor,
 * modifying the array in place.  Demonstrates that RecursiveAction is the
 * right choice when the work has a side effect rather than producing a result.
 */
public class NormaliseAction extends RecursiveAction {

    private static final int THRESHOLD = 500;

    private final double[] data;
    private final int      from, to;
    private final double   factor;

    public NormaliseAction(double[] data, double factor) {
        this(data, 0, data.length, factor);
    }

    private NormaliseAction(double[] data, int from, int to, double factor) {
        this.data   = data;
        this.from   = from;
        this.to     = to;
        this.factor = factor;
    }

    @Override
    protected void compute() {
        int length = to - from;
        if (length <= THRESHOLD) {
            for (int i = from; i < to; i++) data[i] *= factor;
            return;
        }
        int mid = from + length / 2;
        NormaliseAction left  = new NormaliseAction(data, from, mid, factor);
        NormaliseAction right = new NormaliseAction(data, mid,  to,  factor);
        left.fork();
        right.compute();
        left.join();
    }
}
