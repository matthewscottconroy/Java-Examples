package com.generics.wildcards;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        List<Integer> ints    = List.of(10, 20, 30);
        List<Double>  doubles = List.of(1.1, 2.2, 3.3);
        List<String>  strings = List.of("alpha", "beta", "gamma");

        // ----------------------------------------------------------------
        // Invariance: the fundamental problem wildcards solve
        // ----------------------------------------------------------------
        System.out.println("=== Invariance ===");

        // Integer IS-A Number, but List<Integer> IS NOT a List<Number>.
        // The line below would be a compile error:
        //   List<Number> nums = new ArrayList<Integer>();

        // The reason: if it were allowed, you could do:
        //   nums.add(3.14);  // adds a Double into what is really a List<Integer> — heap pollution!

        // Wildcards give us the flexibility we need without breaking safety:
        List<? extends Number> covariant = ints;     // fine — read-only view
        System.out.println("Covariant assignment accepted (read-only): " + covariant);

        // ----------------------------------------------------------------
        // Unbounded wildcard: List<?>
        // ----------------------------------------------------------------
        System.out.println("\n=== Unbounded wildcard: List<?> ===");

        System.out.print("ints:    "); WildcardDemo.printList(ints);
        System.out.print("doubles: "); WildcardDemo.printList(doubles);
        System.out.print("strings: "); WildcardDemo.printList(strings);

        // ----------------------------------------------------------------
        // Upper-bounded wildcard: List<? extends Number>   (PRODUCER)
        // ----------------------------------------------------------------
        System.out.println("\n=== Upper-bounded: List<? extends Number> — PRODUCER ===");

        System.out.println("sum(ints):    " + WildcardDemo.sum(ints));
        System.out.println("sum(doubles): " + WildcardDemo.sum(doubles));
        // WildcardDemo.sum(strings);  // compile error — String not a Number subtype

        // ----------------------------------------------------------------
        // Lower-bounded wildcard: List<? super Integer>   (CONSUMER)
        // ----------------------------------------------------------------
        System.out.println("\n=== Lower-bounded: List<? super Integer> — CONSUMER ===");

        List<Number> numberList = new ArrayList<>();
        List<Object> objectList = new ArrayList<>();

        WildcardDemo.fillWithInts(numberList, 3);   // Number is a supertype of Integer ✓
        WildcardDemo.fillWithInts(objectList, 3);   // Object is a supertype of Integer ✓
        // WildcardDemo.fillWithInts(doubles, 3);   // Double is NOT a supertype of Integer ✗

        System.out.print("numberList after fill: "); WildcardDemo.printList(numberList);
        System.out.print("objectList after fill: "); WildcardDemo.printList(objectList);

        // ----------------------------------------------------------------
        // PECS: copy() — Producer Extends, Consumer Super
        // ----------------------------------------------------------------
        System.out.println("\n=== PECS: copy(src extends T, dest super T) ===");

        List<Integer> source      = List.of(7, 8, 9);
        List<Number>  destination = new ArrayList<>();

        // source  is List<Integer> — Integer extends Number → extends T ✓
        // destination is List<Number>  — Number  is super of Integer → super T ✓
        WildcardDemo.copy(source, destination);
        System.out.print("Copied to List<Number>: "); WildcardDemo.printList(destination);

        // ----------------------------------------------------------------
        // Wildcard capture — swapping without knowing the exact type
        // ----------------------------------------------------------------
        System.out.println("\n=== Wildcard capture and helper ===");

        List<String> mutable = new ArrayList<>(strings);
        System.out.print("Before swapFirst: "); WildcardDemo.printList(mutable);
        WildcardDemo.swapFirst(mutable);
        System.out.print("After  swapFirst: "); WildcardDemo.printList(mutable);

        // ----------------------------------------------------------------
        // Arrays vs. Generics: arrays are COVARIANT (legacy, less safe)
        // ----------------------------------------------------------------
        System.out.println("\n=== Arrays are covariant (contrast with generics) ===");

        // This compiles — arrays allow covariant assignment.
        Number[] numArr = new Integer[3];
        numArr[0] = 1;
        System.out.println("numArr[0] = " + numArr[0]);

        // But this will throw ArrayStoreException at runtime — the array is really Integer[]:
        try {
            numArr[1] = 3.14;   // Double != Integer → ArrayStoreException
        } catch (ArrayStoreException e) {
            System.out.println("ArrayStoreException: " + e.getMessage()
                    + " — arrays pay for covariance at runtime, not compile time");
        }
        // Generics avoid this by being invariant and catching type errors at compile time.
    }
}
