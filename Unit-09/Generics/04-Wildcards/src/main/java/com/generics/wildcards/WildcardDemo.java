package com.generics.wildcards;

import java.util.List;

/**
 * Demonstrates the three wildcard forms and the PECS principle.
 *
 * <p><strong>Why wildcards?</strong>  Generics are <em>invariant</em>:
 * {@code List<Integer>} is NOT a subtype of {@code List<Number>}, even though
 * {@code Integer} is a subtype of {@code Number}.  Wildcards let us write
 * methods that accept a family of parameterized types while preserving safety.
 *
 * <p><strong>PECS — Producer Extends, Consumer Super</strong>:
 * <ul>
 *   <li>If a parameter <em>produces</em> elements you read, use {@code ? extends T}.</li>
 *   <li>If a parameter <em>consumes</em> elements you write, use {@code ? super T}.</li>
 *   <li>If you both read and write, use an exact type parameter ({@code T}).</li>
 * </ul>
 */
public class WildcardDemo {

    // -----------------------------------------------------------------------
    // 1. Unbounded wildcard: List<?>
    //    "Some list, I don't know the element type."
    //    Can only treat elements as Object. Cannot add anything (except null).
    // -----------------------------------------------------------------------
    public static void printList(List<?> list) {
        for (Object item : list) {
            System.out.print(item + " ");
        }
        System.out.println();
    }

    // -----------------------------------------------------------------------
    // 2. Upper-bounded wildcard: List<? extends Number>    (PRODUCER)
    //    "A list of some Number subtype."
    //    READ elements as Number. Cannot safely ADD elements (type unknown).
    // -----------------------------------------------------------------------
    public static double sum(List<? extends Number> numbers) {
        double total = 0;
        for (Number n : numbers) {          // safe: every element IS-A Number
            total += n.doubleValue();
        }
        // numbers.add(1);  // compile error — might be List<Double> and we'd add an Integer
        return total;
    }

    // -----------------------------------------------------------------------
    // 3. Lower-bounded wildcard: List<? super Integer>    (CONSUMER)
    //    "A list that can hold Integers (and possibly wider types)."
    //    ADD Integers safely. Can only READ elements as Object (type unknown).
    // -----------------------------------------------------------------------
    public static void fillWithInts(List<? super Integer> list, int count) {
        for (int i = 1; i <= count; i++) {
            list.add(i);                    // safe: Integer fits in any supertype of Integer
        }
        // Integer n = list.get(0);  // compile error — element might be a Number or Object
    }

    // -----------------------------------------------------------------------
    // 4. PECS in action: copy from producer to consumer.
    //    src  produces elements → extends T
    //    dest consumes elements → super T
    // -----------------------------------------------------------------------
    public static <T> void copy(List<? extends T> src, List<? super T> dest) {
        for (T item : src) {
            dest.add(item);
        }
    }

    // -----------------------------------------------------------------------
    // 5. Wildcard capture + helper method.
    //    The compiler can't reason about List<?> well enough to do a swap,
    //    but a private helper pinning the type to <T> can.
    // -----------------------------------------------------------------------
    public static void swapFirst(List<?> list) {
        swapHelper(list);                   // compiler passes capture to the helper
    }

    private static <T> void swapHelper(List<T> list) {
        if (list.size() < 2) return;
        T tmp = list.get(0);
        list.set(0, list.get(list.size() - 1));
        list.set(list.size() - 1, tmp);
    }
}
