package com.meta.proxy;

import java.util.*;

interface Calculator {
    int add(int a, int b);
    int multiply(int a, int b);
    int expensiveOp(int n);
}

interface FlakyService {
    String fetch(String key);
}

public class Main {

    public static void main(String[] args) {
        System.out.println("=== Dynamic Proxy — Transparent Cross-Cutting Concerns ===\n");

        // ---------------------------------------------------------------
        // 1. Logging proxy
        // ---------------------------------------------------------------
        Calculator real = new Calculator() {
            public int add(int a, int b)      { return a + b; }
            public int multiply(int a, int b) { return a * b; }
            public int expensiveOp(int n)     { return n * n; }
        };

        List<String> log = new ArrayList<>();
        Calculator logged = ServiceProxy.logging(real, Calculator.class, log);

        System.out.println("--- Logging proxy ---");
        logged.add(3, 4);
        logged.multiply(5, 6);
        logged.expensiveOp(7);
        log.forEach(entry -> System.out.println("  " + entry));

        // ---------------------------------------------------------------
        // 2. Timing proxy
        // ---------------------------------------------------------------
        Map<String, Long> timings = new LinkedHashMap<>();
        Calculator timed = ServiceProxy.timing(real, Calculator.class, timings);

        System.out.println("\n--- Timing proxy ---");
        for (int i = 0; i < 1000; i++) timed.add(i, i + 1);
        for (int i = 0; i < 500; i++)  timed.multiply(i, i + 1);
        timings.forEach((method, ns) ->
            System.out.printf("  %-12s  total=%,d ns%n", method, ns));

        // ---------------------------------------------------------------
        // 3. Caching proxy
        // ---------------------------------------------------------------
        int[] callCount = { 0 };
        Calculator countingReal = new Calculator() {
            public int add(int a, int b)      { callCount[0]++; return a + b; }
            public int multiply(int a, int b) { callCount[0]++; return a * b; }
            public int expensiveOp(int n)     { callCount[0]++; return n * n; }
        };

        Calculator cached = ServiceProxy.caching(countingReal, Calculator.class);
        System.out.println("\n--- Caching proxy ---");
        for (int i = 0; i < 5; i++) cached.expensiveOp(42);  // same arg → cached
        System.out.println("  Real method called " + callCount[0] + " time(s) for 5 identical calls");

        // ---------------------------------------------------------------
        // 4. Retry proxy
        // ---------------------------------------------------------------
        int[] failCount = { 0 };
        FlakyService flaky = key -> {
            if (failCount[0]++ < 2) throw new RuntimeException("transient failure");
            return "data:" + key;
        };

        FlakyService retried = ServiceProxy.retrying(flaky, FlakyService.class, 5);
        System.out.println("\n--- Retry proxy (max 5 attempts) ---");
        String result = retried.fetch("orders");
        System.out.println("  Result after " + failCount[0] + " attempts: " + result);

        // ---------------------------------------------------------------
        // 5. Proxy class identity
        // ---------------------------------------------------------------
        System.out.println("\n--- Proxy class identity ---");
        System.out.println("  Is proxy: " + java.lang.reflect.Proxy.isProxyClass(logged.getClass()));
        System.out.println("  Real class: " + real.getClass().getSimpleName());
        System.out.println("  Proxy class: " + logged.getClass().getName());
    }
}
