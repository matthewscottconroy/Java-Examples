package com.meta.proxy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

interface MathService {
    int square(int n);
}

interface Greeter {
    String greet(String name);
}

class DynamicProxyTest {

    MathService realMath = n -> n * n;  // only square; add unused in some tests
    Greeter realGreeter  = name -> "Hello, " + name + "!";

    // -- Logging --

    @Test @DisplayName("Logging proxy records each call")
    void logging_recordsCalls() {
        List<String> log = new ArrayList<>();
        Greeter proxied = ServiceProxy.logging(realGreeter, Greeter.class, log);
        proxied.greet("Alice");
        proxied.greet("Bob");
        assertEquals(2, log.size());
        assertTrue(log.get(0).contains("greet"));
        assertTrue(log.get(0).contains("Alice"));
    }

    @Test @DisplayName("Logging proxy records return value")
    void logging_recordsReturnValue() {
        List<String> log = new ArrayList<>();
        Greeter proxied = ServiceProxy.logging(realGreeter, Greeter.class, log);
        proxied.greet("World");
        assertTrue(log.get(0).contains("Hello, World!"));
    }

    @Test @DisplayName("Logging proxy propagates exception and logs it")
    void logging_propagatesException() {
        Greeter throwing = name -> { throw new IllegalArgumentException("bad name"); };
        List<String> log = new ArrayList<>();
        Greeter proxied = ServiceProxy.logging(throwing, Greeter.class, log);
        assertThrows(IllegalArgumentException.class, () -> proxied.greet("x"));
        assertTrue(log.get(0).contains("ERROR"));
    }

    @Test @DisplayName("Logging proxy does not alter return value")
    void logging_passesResultThrough() {
        List<String> log = new ArrayList<>();
        Greeter proxied = ServiceProxy.logging(realGreeter, Greeter.class, log);
        assertEquals("Hello, Alice!", proxied.greet("Alice"));
    }

    // -- Timing --

    @Test @DisplayName("Timing proxy records method timings")
    void timing_recordsTimings() {
        Map<String, Long> timings = new HashMap<>();
        Greeter proxied = ServiceProxy.timing(realGreeter, Greeter.class, timings);
        proxied.greet("test");
        assertTrue(timings.containsKey("greet"));
        assertTrue(timings.get("greet") >= 0);
    }

    @Test @DisplayName("Timing proxy accumulates time across multiple calls")
    void timing_accumulates() {
        Map<String, Long> timings = new HashMap<>();
        Greeter proxied = ServiceProxy.timing(realGreeter, Greeter.class, timings);
        proxied.greet("a");
        proxied.greet("b");
        long total = timings.get("greet");
        assertTrue(total >= 0);
    }

    @Test @DisplayName("Timing proxy does not alter return value")
    void timing_passesResultThrough() {
        Greeter proxied = ServiceProxy.timing(realGreeter, Greeter.class, new HashMap<>());
        assertEquals("Hello, Tim!", proxied.greet("Tim"));
    }

    // -- Caching --

    @Test @DisplayName("Caching proxy calls real method only once for repeated identical args")
    void caching_callsOnce() {
        AtomicInteger calls = new AtomicInteger();
        Greeter counting = name -> { calls.incrementAndGet(); return "Hi, " + name; };
        Greeter cached = ServiceProxy.caching(counting, Greeter.class);
        cached.greet("Alice");
        cached.greet("Alice");
        cached.greet("Alice");
        assertEquals(1, calls.get(), "Real method should be called only once for same arg");
    }

    @Test @DisplayName("Caching proxy calls real method for different args")
    void caching_differentArgsCauseCalls() {
        AtomicInteger calls = new AtomicInteger();
        Greeter counting = name -> { calls.incrementAndGet(); return "Hi, " + name; };
        Greeter cached = ServiceProxy.caching(counting, Greeter.class);
        cached.greet("Alice");
        cached.greet("Bob");
        assertEquals(2, calls.get());
    }

    @Test @DisplayName("Caching proxy returns same value as uncached")
    void caching_correctResult() {
        Greeter cached = ServiceProxy.caching(realGreeter, Greeter.class);
        assertEquals("Hello, World!", cached.greet("World"));
    }

    // -- Retry --

    @Test @DisplayName("Retry proxy succeeds after transient failures")
    void retry_succeedsAfterFailures() {
        AtomicInteger attempts = new AtomicInteger();
        Greeter flaky = name -> {
            if (attempts.incrementAndGet() < 3) throw new RuntimeException("transient");
            return "Hello, " + name;
        };
        Greeter retried = ServiceProxy.retrying(flaky, Greeter.class, 5);
        assertEquals("Hello, Alice", retried.greet("Alice"));
        assertEquals(3, attempts.get());
    }

    @Test @DisplayName("Retry proxy throws after exhausting attempts")
    void retry_throwsWhenExhausted() {
        Greeter alwaysFails = name -> { throw new RuntimeException("always fails"); };
        Greeter retried = ServiceProxy.retrying(alwaysFails, Greeter.class, 3);
        assertThrows(RuntimeException.class, () -> retried.greet("x"));
    }

    // -- Proxy identity --

    @Test @DisplayName("Proxy.isProxyClass() identifies generated proxy")
    void proxyClass_identified() {
        Greeter proxied = ServiceProxy.logging(realGreeter, Greeter.class, new ArrayList<>());
        assertTrue(Proxy.isProxyClass(proxied.getClass()));
        assertFalse(Proxy.isProxyClass(realGreeter.getClass()));
    }
}
