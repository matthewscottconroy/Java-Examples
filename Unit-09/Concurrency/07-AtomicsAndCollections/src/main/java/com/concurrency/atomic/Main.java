package com.concurrency.atomic;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("=== Atomic Variables ===");
        AtomicDemo.showAtomicInteger();
        AtomicDemo.showLongAdder();
        AtomicDemo.showAtomicReference();

        System.out.println("\n=== Concurrent Collections ===");
        ConcurrentCollections.showConcurrentHashMap();
        ConcurrentCollections.showCopyOnWrite();
        ConcurrentCollections.showBlockingQueue();
    }
}
