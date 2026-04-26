package com.generics.box;

public class Main {

    public static void main(String[] args) {

        // ----------------------------------------------------------------
        // PART 1: The problem — ObjectBox requires manual casts
        // ----------------------------------------------------------------
        System.out.println("=== ObjectBox (pre-generics style) ===");

        ObjectBox strObjBox = new ObjectBox("hello");
        ObjectBox intObjBox = new ObjectBox(42);

        // The compiler sees Object, so we must cast manually.
        String s = (String) strObjBox.get();
        int    n = (Integer) intObjBox.get();   // Integer auto-unboxed to int
        System.out.println("Retrieved: " + s + ", " + n);

        // This compiles, but crashes at runtime with ClassCastException.
        // Uncomment to see the failure:
        //   Integer bad = (Integer) strObjBox.get();

        // Nothing stops mixing types — the compiler accepts this:
        strObjBox.set(999);     // was a String, now an Integer — no warning
        System.out.println("After set(999): " + strObjBox.get());

        // ----------------------------------------------------------------
        // PART 2: The solution — Box<T> locks in the type at compile time
        // ----------------------------------------------------------------
        System.out.println("\n=== Box<T> (generic) ===");

        Box<String>  boxOfString = new Box<>("hello");
        Box<Integer> boxOfInt    = new Box<>(42);

        // get() already returns String / Integer — no cast required.
        String text   = boxOfString.get();
        int    number = boxOfInt.get();     // Integer auto-unboxed
        System.out.println("String box: " + text);
        System.out.println("Integer box: " + number);

        // The compiler rejects a wrong type at the assignment site, not at runtime:
        //   boxOfString.set(999);   // compile error: int is not String

        // ----------------------------------------------------------------
        // PART 3: Diamond operator — Java infers the type argument (Java 7+)
        // ----------------------------------------------------------------
        System.out.println("\n=== Diamond operator ===");

        // Both sides agree on <Double>; writing it twice would be redundant.
        Box<Double> boxOfDouble = new Box<>(3.14);
        System.out.println(boxOfDouble);

        // ----------------------------------------------------------------
        // PART 4: Type parameters compose — boxes of boxes
        // ----------------------------------------------------------------
        System.out.println("\n=== Nested generics ===");

        Box<Box<String>> nested = new Box<>(new Box<>("deep value"));
        System.out.println("Outer: " + nested);
        System.out.println("Inner value: " + nested.get().get());

        // ----------------------------------------------------------------
        // PART 5: Raw types — the old way, avoid in new code
        // ----------------------------------------------------------------
        System.out.println("\n=== Raw types (avoid) ===");

        // A raw type omits the type argument entirely.
        // The compiler issues an "unchecked" warning — suppressed here for demo only.
        @SuppressWarnings({"unchecked", "rawtypes"})
        Box rawBox = new Box("raw");
        Object rawValue = rawBox.get();     // returns Object, just like ObjectBox
        System.out.println("Raw box value: " + rawValue);
    }
}
