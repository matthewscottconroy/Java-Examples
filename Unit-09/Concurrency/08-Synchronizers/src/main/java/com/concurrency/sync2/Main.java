package com.concurrency.sync2;

public class Main {

    public static void main(String[] args) throws Exception {

        SynchronizerDemo.showCountDownLatch();
        SynchronizerDemo.showCyclicBarrier();
        SynchronizerDemo.showSemaphore();
        SynchronizerDemo.showExchanger();
    }
}
