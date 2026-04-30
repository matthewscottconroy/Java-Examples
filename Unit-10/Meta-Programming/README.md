# Meta-Programming & Recursion

Eight self-contained Maven modules covering the mechanisms Java provides for programs that inspect, transform, or generate other programs at runtime, together with the recursive patterns that make those mechanisms useful.

Each module is independent — compile and run it on its own with `mvn exec:java` or explore the concepts through the test suite (`mvn test`).

---

## Modules

| # | Directory | Topic |
|---|-----------|-------|
| 01 | [Recursive-Structures](01-Recursive-Structures/) | Immutable persistent `FList<A>` and `BTree<A>` built from sealed interfaces and structural recursion |
| 02 | [Recursive-Descent-Parser](02-Recursive-Descent-Parser/) | Hand-written LL(1) parser for arithmetic expressions; each grammar rule maps to one method |
| 03 | [Trampolining](03-Trampolining/) | Stack-safe deep recursion via a trampoline loop and continuation-passing Fibonacci |
| 04 | [Mutual-Recursion](04-Mutual-Recursion/) | `isEven`/`isOdd`, Hofstadter M/F sequences, a tokeniser, and a recursive-descent balanced-parens checker |
| 05 | [Reflection](05-Reflection/) | `Class.forName`, field/method access, private field reading, and a runtime plug-in registry |
| 06 | [Dynamic-Proxy](06-Dynamic-Proxy/) | `Proxy.newProxyInstance` + `InvocationHandler` to add logging, timing, caching, and retry to any interface |
| 07 | [Annotations](07-Annotations/) | Custom `@Required`, `@Range`, and `@Pattern` annotations processed by a reflection-based validator |
| 08 | [Method-Handles](08-Method-Handles/) | `MethodHandle` lookups, `filterArguments`, `filterReturnValue`, `bindTo`, and `guardWithTest` combinators |

---

## Running a module

```bash
cd 01-Recursive-Structures
mvn exec:java          # run Main
mvn test               # run the test suite
```

All modules require **Java 21** and **Maven 3.6+**.

---

## Conceptual map

```
                 ┌─────────────────────────────────┐
                 │         Recursion (01–04)         │
                 │                                   │
                 │  01 Structural recursion on ADTs  │
                 │  02 Recursive descent parsing     │
                 │  03 Trampolining (CPS)            │
                 │  04 Mutual recursion              │
                 └──────────────┬──────────────────┘
                                │ enables
                 ┌──────────────▼──────────────────┐
                 │       Reflection (05–06)          │
                 │                                   │
                 │  05 Inspect and invoke at runtime │
                 │  06 Dynamic proxies               │
                 └──────────────┬──────────────────┘
                                │ builds on
                 ┌──────────────▼──────────────────┐
                 │   Metadata & Low-Level (07–08)    │
                 │                                   │
                 │  07 Runtime annotation processing │
                 │  08 Method handles & combinators  │
                 └─────────────────────────────────┘
```

---

## Key Java features used

- **Sealed interfaces + records** — algebraic data types (`FList`, `BTree`, `Expr`, `Bounce`, `Token`)
- **Pattern matching in switch** — with `when` guards for value-based dispatch (Java 21)
- **`java.lang.reflect`** — `Class`, `Field`, `Method`, `Constructor`, `Proxy`, `InvocationHandler`
- **`java.lang.invoke`** — `MethodHandle`, `MethodHandles.Lookup`, `MethodType`, combinators
- **`@Retention(RUNTIME)`** — annotations visible to reflection at runtime
- **Trampoline / CPS** — turn tail-recursive functions into iterative loops without stack growth
