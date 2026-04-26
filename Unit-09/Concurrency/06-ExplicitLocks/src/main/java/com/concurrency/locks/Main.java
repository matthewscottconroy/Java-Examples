package com.concurrency.locks;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("=== ReentrantLock ===");
        ReentrantLockDemo.showTryLock();
        ReentrantLockDemo.showReentrancy();

        System.out.println("\n=== ReadWriteLock + StampedLock ===");
        ReadWriteDemo.showReadWriteLock();
        ReadWriteDemo.showStampedLock();

        System.out.println("\n=== Condition (explicit wait sets) ===");
        System.out.println("Same bounded-buffer pattern as wait/notify, but with two separate");
        System.out.println("Conditions: notFull (producers wait) and notEmpty (consumers wait).");
        ConditionDemo.demonstrate();
    }
}
