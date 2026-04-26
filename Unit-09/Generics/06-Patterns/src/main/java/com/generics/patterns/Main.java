package com.generics.patterns;

import java.util.function.UnaryOperator;

public class Main {

    public static void main(String[] args) throws Exception {

        // ----------------------------------------------------------------
        // PART 1: Self-bounded fluent builder hierarchy
        // ----------------------------------------------------------------
        System.out.println("=== Self-bounded builder: AbstractBuilder<B extends AbstractBuilder<B>> ===");

        // AnimalBuilder inherits name() and description() from AbstractBuilder.
        // Because of the self-bound, those inherited setters return AnimalBuilder —
        // not the abstract base — so .sound() is reachable without a cast.
        FluentBuilder.Animal cat = new FluentBuilder.AnimalBuilder()
                .name("Cat")
                .description("Domestic feline")
                .sound("Meow")           // subclass-specific setter — still fluent
                .build();

        FluentBuilder.Vehicle car = new FluentBuilder.VehicleBuilder()
                .name("Sports Car")
                .description("Two-door coupe")
                .horsepower(400)         // VehicleBuilder-specific
                .build();

        System.out.println(cat);
        System.out.println(car);

        // Without the self-bound, calling an inherited setter first would return
        // AbstractBuilder, and the compiler would reject the subclass-specific call:
        //   new AnimalBuilder().name("Cat").sound("Meow")  // sound() not on AbstractBuilder
        // The self-bound solves this by making name() return B = AnimalBuilder.

        // ----------------------------------------------------------------
        // PART 2: Generic singleton factory
        // ----------------------------------------------------------------
        System.out.println("\n=== Generic singleton: one instance, any type parameter ===");

        // identityOperator() returns the same object each time, but typed differently.
        UnaryOperator<String>  strId = GenericSingleton.identityOperator();
        UnaryOperator<Integer> intId = GenericSingleton.identityOperator();

        System.out.println("strId.apply(\"hello\") = " + strId.apply("hello"));
        System.out.println("intId.apply(42)       = " + intId.apply(42));
        // Cast to Object first — the compiler can't compare differently-parameterized
        // types directly with ==, since their static types are unrelated.
        System.out.println("Same instance?        " + ((Object) strId == (Object) intId));   // true

        // EmptyWrapper — mirrors Collections.emptyList() / emptySet() style
        var strWrapper = GenericSingleton.EmptyWrapper.<String>getInstance();
        var intWrapper = GenericSingleton.EmptyWrapper.<Integer>getInstance();
        System.out.println("strWrapper: " + strWrapper + "  isEmpty=" + strWrapper.isEmpty());
        System.out.println("Same EmptyWrapper? " + ((Object) strWrapper == (Object) intWrapper));

        // ----------------------------------------------------------------
        // PART 3: Memoizing factory (type token + singleton cache)
        // ----------------------------------------------------------------
        System.out.println("\n=== Memoizing factory: getOrCreate(Class<T>) ===");

        StringBuilder sb1 = GenericSingleton.getOrCreate(StringBuilder.class);
        StringBuilder sb2 = GenericSingleton.getOrCreate(StringBuilder.class);
        System.out.println("Same StringBuilder instance? " + (sb1 == sb2));  // true

        sb1.append("mutated via sb1");
        System.out.println("sb2 sees the mutation: \"" + sb2 + "\"");  // same object

        // ----------------------------------------------------------------
        // Summary of patterns seen across examples 01–06
        // ----------------------------------------------------------------
        System.out.println("\n=== Concepts covered across all six examples ===");
        System.out.println("01  Box<T>                  — generic class, type safety, diamond op");
        System.out.println("02  <T> T method(T)         — generic methods, Pair<K,V>, inference");
        System.out.println("03  <T extends Number>      — upper bounds, recursive & multi-bounds");
        System.out.println("04  List<? extends Number>  — wildcards, PECS, invariance");
        System.out.println("05  erasure + type tokens   — runtime view, bridge methods, Class<T>");
        System.out.println("06  Builder<B extends B>    — self-bounded fluent API, singleton factory");
    }
}
