package com.lambda.func;

import java.util.function.*;

/**
 * The core functional interfaces from {@code java.util.function}.
 *
 * <p>A lambda expression is an instance of a <em>functional interface</em> —
 * any interface with exactly one abstract method.  The target type is inferred
 * from context; the same lambda body can satisfy different interface types.
 *
 * <p>The standard library provides ~43 pre-defined functional interfaces.
 * These are the eight you will use most often:
 *
 * <pre>
 *   Function&lt;T, R&gt;     T → R          transform
 *   Predicate&lt;T&gt;        T → boolean    test
 *   Consumer&lt;T&gt;         T → void       side effect
 *   Supplier&lt;T&gt;         () → T         produce
 *   UnaryOperator&lt;T&gt;    T → T          transform in-place (Function specialisation)
 *   BinaryOperator&lt;T&gt;   (T,T) → T      combine two into one
 *   BiFunction&lt;T,U,R&gt;   (T,U) → R      two inputs
 *   BiPredicate&lt;T,U&gt;    (T,U) → boolean
 * </pre>
 */
public class BuiltInInterfaces {

    public static void demonstrate() {
        System.out.println("-- Function<T, R>: transform --");
        Function<String, Integer> length  = s -> s.length();
        Function<String, String>  shout   = s -> s.toUpperCase() + "!";
        System.out.println("  length(\"hello\") = " + length.apply("hello"));
        System.out.println("  shout(\"hello\")  = " + shout.apply("hello"));

        System.out.println("\n-- Predicate<T>: test --");
        Predicate<String> isLong   = s -> s.length() > 5;
        Predicate<Integer> isEven  = n -> n % 2 == 0;
        System.out.println("  isLong(\"hi\"):       " + isLong.test("hi"));
        System.out.println("  isLong(\"hellooo\"):  " + isLong.test("hellooo"));
        System.out.println("  isEven(4):           " + isEven.test(4));
        System.out.println("  isEven.negate()(4):  " + isEven.negate().test(4));

        System.out.println("\n-- Consumer<T>: side effect --");
        Consumer<String> print  = s -> System.out.println("  consumed: " + s);
        Consumer<String> audit  = s -> System.out.println("  audit:    " + s.toUpperCase());
        print.accept("hello");
        print.andThen(audit).accept("chained");   // andThen: run both in sequence

        System.out.println("\n-- Supplier<T>: produce --");
        Supplier<Double> random = Math::random;
        Supplier<String> greet  = () -> "Hello at " + System.currentTimeMillis();
        System.out.println("  random:  " + String.format("%.4f", random.get()));
        System.out.println("  greet:   " + greet.get());

        System.out.println("\n-- UnaryOperator<T> and BinaryOperator<T> --");
        UnaryOperator<String>  trim    = String::trim;
        BinaryOperator<String> concat  = (a, b) -> a + b;
        System.out.println("  trim(\"  hi  \"):       \"" + trim.apply("  hi  ") + "\"");
        System.out.println("  concat(\"foo\",\"bar\"): " + concat.apply("foo", "bar"));

        System.out.println("\n-- BiFunction<T,U,R> --");
        BiFunction<String, Integer, String> repeat = (s, n) -> s.repeat(n);
        System.out.println("  repeat(\"ab\", 3): " + repeat.apply("ab", 3));

        System.out.println("\n-- Same body, different interface types --");
        // The lambda  x -> x * 2  is valid for any of these:
        Function<Integer, Integer> f = x -> x * 2;
        UnaryOperator<Integer>     u = x -> x * 2;
        IntUnaryOperator           i = x -> x * 2;   // primitive specialisation — no boxing
        System.out.println("  Function:           " + f.apply(5));
        System.out.println("  UnaryOperator:      " + u.apply(5));
        System.out.println("  IntUnaryOperator:   " + i.applyAsInt(5));
    }
}
