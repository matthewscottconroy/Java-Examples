package com.lambda.capture;

import java.util.List;
import java.util.function.*;

/**
 * Function composition — building pipelines from small, single-purpose functions.
 *
 * <p>The java.util.function interfaces include default composition methods so
 * that functions can be connected without needing to write wrapper lambdas:
 *
 * <ul>
 *   <li>{@code Function.andThen(f)} — apply this, then f.  Left to right.</li>
 *   <li>{@code Function.compose(f)} — apply f first, then this.  Right to left.</li>
 *   <li>{@code Predicate.and/or/negate} — boolean algebra on predicates.</li>
 *   <li>{@code Consumer.andThen} — chain side effects in order.</li>
 * </ul>
 *
 * <p>Composition is the functional-programming analogue of the Builder or
 * Decorator pattern: you assemble behaviour from pieces at runtime rather than
 * encoding it in a fixed class hierarchy.
 */
public class CompositionDemo {

    public void showFunctionComposition() {
        System.out.println("-- Function.andThen / compose --");

        Function<String, String> trim   = String::trim;
        Function<String, String> upper  = String::toUpperCase;
        Function<String, Integer> length = String::length;

        // andThen: trim → upper → length  (left-to-right reading order)
        Function<String, Integer> pipeline = trim.andThen(upper).andThen(length);
        System.out.println("  pipeline(\"  hello  \") = " + pipeline.apply("  hello  ")); // 5

        // compose: same result, written right-to-left (mathematical notation)
        // length ∘ upper ∘ trim = length.compose(upper).compose(trim)
        Function<String, Integer> composed = length.compose(upper).compose(trim);
        System.out.println("  composed(\"  hello  \") = " + composed.apply("  hello  ")); // 5

        // Ordering matters — different pipelines, different results.
        Function<String, String> trimThenUpper = trim.andThen(upper);
        Function<String, String> upperThenTrim = upper.andThen(trim);
        System.out.println("  trimThenUpper(\"  hello  \") = \"" + trimThenUpper.apply("  hello  ") + "\"");
        System.out.println("  upperThenTrim(\"  hello  \") = \"" + upperThenTrim.apply("  hello  ") + "\"");
    }

    public void showPredicateComposition() {
        System.out.println("\n-- Predicate.and / or / negate --");

        Predicate<Integer> positive  = n -> n > 0;
        Predicate<Integer> even      = n -> n % 2 == 0;
        Predicate<Integer> small     = n -> n < 100;

        Predicate<Integer> positiveAndEven = positive.and(even);
        Predicate<Integer> positiveOrEven  = positive.or(even);
        Predicate<Integer> notSmall        = small.negate();

        List<Integer> nums = List.of(-4, -1, 0, 1, 2, 6, 50, 101, 200);

        System.out.print("  positiveAndEven: ");
        nums.stream().filter(positiveAndEven).forEach(n -> System.out.print(n + " "));

        System.out.print("\n  positiveOrEven:  ");
        nums.stream().filter(positiveOrEven).forEach(n -> System.out.print(n + " "));

        System.out.print("\n  notSmall:        ");
        nums.stream().filter(notSmall).forEach(n -> System.out.print(n + " "));
        System.out.println();

        // Predicate.not() wraps a method reference — handy with streams.
        List<String> words = List.of("hello", "", "world", "  ", "java");
        System.out.print("  non-blank words: ");
        words.stream().filter(Predicate.not(String::isBlank))
             .forEach(w -> System.out.print("\"" + w + "\" "));
        System.out.println();
    }

    public void showConsumerChaining() {
        System.out.println("\n-- Consumer.andThen (chaining side effects) --");

        Consumer<String> print  = s -> System.out.println("    print:  " + s);
        Consumer<String> log    = s -> System.out.println("    log:    " + s.toUpperCase());
        Consumer<String> audit  = s -> System.out.println("    audit:  length=" + s.length());

        Consumer<String> all = print.andThen(log).andThen(audit);
        System.out.println("  processing \"lambda\":");
        all.accept("lambda");
    }

    public void showUnaryOperatorComposition() {
        System.out.println("\n-- UnaryOperator composition (integer transforms) --");

        UnaryOperator<Integer> times2  = n -> n * 2;
        UnaryOperator<Integer> plus10  = n -> n + 10;
        UnaryOperator<Integer> squared = n -> n * n;

        // andThen works on UnaryOperator too (it inherits from Function).
        Function<Integer, Integer> pipeline = times2.andThen(plus10).andThen(squared);
        // (5 * 2 + 10)^2 = 20^2 = 400
        System.out.println("  pipeline(5) = " + pipeline.apply(5));

        // Build a list of transforms and fold them into a single function.
        List<UnaryOperator<Integer>> ops = List.of(times2, plus10, squared);
        Function<Integer, Integer> combined = ops.stream()
            .reduce(Function.identity(), Function::andThen, Function::andThen);
        System.out.println("  folded pipeline(5) = " + combined.apply(5));
    }
}
