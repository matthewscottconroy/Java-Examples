package com.generics.methods;

public class Main {

    public static void main(String[] args) {

        // ----------------------------------------------------------------
        // PART 1: Type inference — compiler reads the argument to fill in T
        // ----------------------------------------------------------------
        System.out.println("=== identity() — type inference ===");

        String  s = GenericMethods.identity("hello");   // T inferred as String
        int     n = GenericMethods.identity(42);        // T = Integer, auto-unboxed
        double  d = GenericMethods.identity(3.14);
        System.out.println(s + ", " + n + ", " + d);

        // ----------------------------------------------------------------
        // PART 2: Generic array utilities
        // ----------------------------------------------------------------
        System.out.println("\n=== printArray() and swap() ===");

        Integer[] ints    = {3, 1, 4, 1, 5, 9};
        String[]  words   = {"cherry", "apple", "fig", "banana"};
        Double[]  doubles = {2.7, 1.4, 3.1};

        System.out.print("Original ints:  "); GenericMethods.printArray(ints);
        GenericMethods.swap(ints, 0, 5);
        System.out.print("After swap(0,5): "); GenericMethods.printArray(ints);

        System.out.print("String array: "); GenericMethods.printArray(words);
        System.out.print("Double array: "); GenericMethods.printArray(doubles);

        // ----------------------------------------------------------------
        // PART 3: Multiple type parameters — Pair<K, V>
        // ----------------------------------------------------------------
        System.out.println("\n=== Pair<K, V> — two independent type parameters ===");

        // K = String, V = Integer — different types
        Pair<String, Integer> nameAge  = GenericMethods.pairOf("Alice", 30);
        System.out.println("Pair:    " + nameAge);
        System.out.println("Swapped: " + nameAge.swap());   // returns Pair<Integer, String>

        // K = V = String — same type is fine too
        Pair<String, String> greeting = GenericMethods.duplicate("hello");
        System.out.println("Duplicate: " + greeting);

        // Nested: Pair whose first element is itself a Pair
        Pair<Pair<String, Integer>, Double> complex =
                GenericMethods.pairOf(nameAge, 98.6);
        System.out.println("Complex pair: " + complex);

        // ----------------------------------------------------------------
        // PART 4: Bounded type parameter preview (covered fully in example 03)
        // ----------------------------------------------------------------
        System.out.println("\n=== max() — bounded type parameter ===");

        System.out.println("max(3, 7)          = " + GenericMethods.max(3, 7));
        System.out.println("max(\"apple\",\"fig\") = " + GenericMethods.max("apple", "fig"));
        System.out.println("max(1.5, 2.5)      = " + GenericMethods.max(1.5, 2.5));

        // ----------------------------------------------------------------
        // PART 5: Explicit type witness — pin T when inference can't
        // ----------------------------------------------------------------
        System.out.println("\n=== Explicit type witness ===");

        // Inference works in most cases — this is just for illustration.
        // The <String> before the method name is the type witness.
        var emptyStrings  = GenericMethods.<String>emptyList();
        var emptyIntegers = GenericMethods.<Integer>emptyList();

        System.out.println("Empty string list:  " + emptyStrings);
        System.out.println("Empty integer list: " + emptyIntegers);
        System.out.println("Both are empty? " + (emptyStrings.isEmpty() && emptyIntegers.isEmpty()));
    }
}
