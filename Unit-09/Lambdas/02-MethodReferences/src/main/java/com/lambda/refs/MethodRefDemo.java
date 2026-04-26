package com.lambda.refs;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * The four kinds of method reference.
 *
 * <p>A method reference is shorthand for a lambda that delegates immediately
 * to an existing method.  {@code ClassName::methodName} is not a method call —
 * it is a reference that can be stored in a functional-interface variable.
 *
 * <pre>
 *   Kind                     Syntax                     Lambda equivalent
 *   ─────────────────────────────────────────────────────────────────────
 *   1. Static                ClassName::staticMethod    x -> ClassName.method(x)
 *   2. Bound instance        obj::instanceMethod        x -> obj.method(x)
 *   3. Unbound instance      ClassName::instanceMethod  (obj, x) -> obj.method(x)
 *   4. Constructor           ClassName::new             args -> new ClassName(args)
 * </pre>
 *
 * <p>The method reference must match the functional interface's signature
 * after erasing one implicit parameter (the receiver for instance methods).
 */
public class MethodRefDemo {

    record Person(String name, int age) implements Comparable<Person> {
        @Override public int compareTo(Person o) { return Integer.compare(this.age, o.age); }
    }

    // -----------------------------------------------------------------------
    // 1. Static method reference: ClassName::staticMethod
    //    Lambda: x -> Integer.parseInt(x)
    // -----------------------------------------------------------------------
    public static void showStatic() {
        System.out.println("-- Static method reference --");
        Function<String, Integer> parse1 = s -> Integer.parseInt(s);   // lambda
        Function<String, Integer> parse2 = Integer::parseInt;           // reference
        System.out.println("  parseInt(\"42\") via lambda: " + parse1.apply("42"));
        System.out.println("  parseInt(\"42\") via ref:    " + parse2.apply("42"));

        // Sorting with a static comparator.
        List<String> names = new ArrayList<>(List.of("Charlie", "alice", "Bob"));
        names.sort(String::compareToIgnoreCase);   // BiFunction<String,String,int>
        System.out.println("  sorted case-insensitive: " + names);

        Predicate<Object> isNull = Objects::isNull;
        System.out.println("  isNull(null): " + isNull.test(null));
        System.out.println("  isNull(\"x\"):  " + isNull.test("x"));
    }

    // -----------------------------------------------------------------------
    // 2. Bound instance reference: obj::instanceMethod
    //    Lambda: x -> prefix.concat(x)  — the receiver is captured
    // -----------------------------------------------------------------------
    public static void showBound() {
        System.out.println("\n-- Bound instance reference --");
        String prefix = "Hello, ";
        Function<String, String> greet1 = name -> prefix.concat(name);  // lambda
        Function<String, String> greet2 = prefix::concat;               // reference

        System.out.println("  lambda: " + greet1.apply("world"));
        System.out.println("  ref:    " + greet2.apply("world"));

        // System.out::println is the canonical example.
        Consumer<String> log = System.out::println;
        log.accept("  logged via System.out::println");
    }

    // -----------------------------------------------------------------------
    // 3. Unbound instance reference: ClassName::instanceMethod
    //    Lambda: (obj, arg) -> obj.method(arg) — the first parameter IS the receiver
    // -----------------------------------------------------------------------
    public static void showUnbound() {
        System.out.println("\n-- Unbound instance reference --");
        // String::toUpperCase: (String s) -> s.toUpperCase()
        Function<String, String> upper1 = s -> s.toUpperCase();    // lambda
        Function<String, String> upper2 = String::toUpperCase;     // reference
        System.out.println("  lambda: " + upper1.apply("hello"));
        System.out.println("  ref:    " + upper2.apply("hello"));

        // String::compareToIgnoreCase: (String a, String b) -> a.compareToIgnoreCase(b)
        Comparator<String> cmp = String::compareToIgnoreCase;
        List<String> words = new ArrayList<>(List.of("Banana", "apple", "Cherry"));
        words.sort(cmp);
        System.out.println("  sorted: " + words);

        // Person::compareTo — compareTo(Person) is an instance method on Person
        List<Person> people = new ArrayList<>(List.of(
                new Person("Bob", 35), new Person("Alice", 28), new Person("Charlie", 42)));
        people.sort(Person::compareTo);
        System.out.println("  people by age: " + people);
    }

    // -----------------------------------------------------------------------
    // 4. Constructor reference: ClassName::new
    //    Lambda: args -> new ClassName(args)
    // -----------------------------------------------------------------------
    public static void showConstructor() {
        System.out.println("\n-- Constructor reference --");
        // Supplier<List<String>>: () -> new ArrayList<>()
        Supplier<List<String>>        makeList = ArrayList::new;
        // Function<String, Person>: s -> new StringBuilder(s)
        Function<String, StringBuilder> makeSb = StringBuilder::new;
        // BiFunction<String, Integer, Person>: (n, a) -> new Person(n, a)
        BiFunction<String, Integer, Person> makePerson = Person::new;

        List<String> list = makeList.get();
        list.add("alpha"); list.add("beta");
        System.out.println("  new ArrayList via ref: " + list);
        System.out.println("  new StringBuilder(\"hi\"): " + makeSb.apply("hi"));

        Person p = makePerson.apply("Diana", 29);
        System.out.println("  new Person(\"Diana\",29): " + p);

        // Constructor refs shine in stream collect/map:
        List<String> names = List.of("eve", "frank");
        List<StringBuilder> builders = names.stream()
                .map(StringBuilder::new)
                .collect(Collectors.toList());
        System.out.println("  stream map(StringBuilder::new): " + builders);
    }
}
