# 08 — Method Handles

`java.lang.invoke.MethodHandle` is a typed, directly-executable reference to any method, constructor, or field. It sits between reflection (flexible, slow) and compiled bytecode (fast, rigid): the JVM can inline and optimise handles held in `static final` fields.

## Lookup

```java
MethodHandles.Lookup lookup = MethodHandles.lookup();
MethodHandle mh = lookup.findVirtual(String.class, "toUpperCase",
    MethodType.methodType(String.class));
String result = (String) mh.invoke("hello");  // → "HELLO"
```

`findVirtual`, `findStatic`, `findConstructor` — the three main lookup kinds.

## Combinators

| Combinator | Effect |
|-----------|--------|
| `filterArguments(target, pos, filters…)` | Pre-process arguments before calling target |
| `filterReturnValue(target, filter)` | Post-process the return value |
| `bindTo(receiver)` | Partial application — fix the first argument |
| `guardWithTest(test, ifTrue, ifFalse)` | Inline branch based on a predicate handle |

These compose handles without writing new classes.

## `invoke` vs `invokeExact`

- `invoke` — accepts widening/narrowing conversions; simpler, slightly slower
- `invokeExact` — requires exact type match; faster, throws `WrongMethodTypeException` on mismatch

## Run

```bash
mvn exec:java   # demonstrates all combinators and functional-interface wrapping
mvn test
```
