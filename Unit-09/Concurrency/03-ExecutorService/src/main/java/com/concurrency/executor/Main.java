package com.concurrency.executor;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("=== Pool Types ===");
        PoolTypes.showFixed();
        PoolTypes.showCached();
        PoolTypes.showSingle();

        System.out.println("\n=== ScheduledExecutorService ===");
        ScheduledDemo.demonstrate();

        System.out.println("\n=== ThreadPoolExecutor Internals ===");
        PoolInternals.showCallerRunsPolicy();
        PoolInternals.showPoolStats();
    }
}
