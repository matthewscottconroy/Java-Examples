package com.lambda.capture;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

/**
 * Lambda capture rules and closure semantics.
 *
 * <p>A lambda can read any local variable from its enclosing scope, but only
 * if that variable is effectively final — assigned exactly once, never mutated.
 * This rule exists because local variables live on the stack frame of the
 * enclosing method; if the lambda outlives that frame (e.g., handed to another
 * thread) and the variable were mutable, you'd have a data race with no lock.
 *
 * <p>Instance fields and static fields are NOT restricted — they live on the
 * heap and the lambda holds a reference to the enclosing object (or the class),
 * so mutation is always possible (though not always safe in concurrent code).
 */
public class CaptureDemo {

    private int instanceField = 10;   // heap — lambdas can read and write freely
    private static int staticField = 20;

    public void showEffectivelyFinal() {
        System.out.println("-- Effectively-final capture --");

        String prefix = "Result: ";        // effectively final — never reassigned
        // prefix = "changed";             // uncommenting this breaks the lambda below

        Function<Integer, String> format = n -> prefix + n;
        System.out.println("  " + format.apply(42));

        // The compiler inlines the value or captures a copy; either way the
        // variable's identity is frozen at the moment the lambda is created.
        int base = 100;
        IntUnaryOperator addBase = n -> n + base;
        System.out.println("  addBase(5) = " + addBase.applyAsInt(5));
    }

    public void showInstanceCapture() {
        System.out.println("\n-- Instance and static field capture --");

        // 'this' is captured implicitly — the lambda holds a reference to this CaptureDemo.
        Supplier<Integer> getField = () -> this.instanceField;
        Runnable bump = () -> this.instanceField++;  // mutation is allowed

        System.out.println("  before bump: " + getField.get());
        bump.run();
        bump.run();
        System.out.println("  after two bumps: " + getField.get());

        // Static field — no 'this' needed.
        Supplier<Integer> getStatic = () -> staticField;
        System.out.println("  static field: " + getStatic.get());
    }

    public void showClosurePitfall() {
        System.out.println("\n-- Closure pitfall: shared mutable container --");

        // Workaround people use when they need mutation: put the value inside
        // a container that IS effectively final (the reference doesn't change,
        // only the object's contents do).  This is correct but introduces
        // shared mutable state — use carefully and not in concurrent contexts.
        int[] counter = {0};  // array ref is effectively final; counter[0] is mutable
        Runnable inc = () -> counter[0]++;
        inc.run(); inc.run(); inc.run();
        System.out.println("  counter after 3 runs: " + counter[0]);

        // A more readable alternative is AtomicInteger.
        var atomicCount = new java.util.concurrent.atomic.AtomicInteger();
        Runnable atomicInc = atomicCount::incrementAndGet;
        atomicInc.run(); atomicInc.run();
        System.out.println("  atomic after 2 runs: " + atomicCount.get());
    }

    public void showLambdaLifetime() {
        System.out.println("\n-- Lambda lifetime: outliving the enclosing scope --");

        // The lambda is stored in a list and called after makeSuppliers() returns.
        // 'i' is captured by value (a copy) at each iteration — each lambda
        // holds its own snapshot.  This is why effectively-final matters: if 'i'
        // could be mutated after capture, all lambdas would see the final value.
        List<Supplier<Integer>> suppliers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int captured = i;           // new effectively-final variable each iteration
            suppliers.add(() -> captured);
        }

        System.out.print("  values: ");
        suppliers.forEach(s -> System.out.print(s.get() + " "));
        System.out.println();

        // Compare with the classic broken pattern in pre-Java-8 anonymous classes:
        // using 'i' directly inside a loop body was legal but gave surprising results.
    }
}
