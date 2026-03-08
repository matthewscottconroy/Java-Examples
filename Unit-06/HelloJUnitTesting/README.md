# HelloJUnitTesting

Demonstrates JUnit 5 unit testing: how to write tests, what assertions do,
and how Maven discovers and runs them.

---

## What is a Unit Test?

A unit test is a small piece of code that calls one method with known inputs
and checks that the output matches what you expect.  If someone changes the
code and breaks it, the test fails immediately — before the bug reaches users.

---

## JUnit 5 Annotations

| Annotation       | Purpose                                                   |
|------------------|-----------------------------------------------------------|
| `@Test`          | Marks a method as a test case                             |
| `@DisplayName`   | Human-readable name shown in test reports                 |
| `@BeforeEach`    | Runs before every test (useful for resetting state)       |
| `@AfterEach`     | Runs after every test                                     |
| `@Disabled`      | Skips a test (with a reason)                              |

---

## Common Assertions

```java
assertEquals(expected, actual)          // values are equal
assertNotEquals(a, b)                   // values are not equal
assertTrue(condition)                   // condition is true
assertFalse(condition)                  // condition is false
assertNull(obj)                         // reference is null
assertNotNull(obj)                      // reference is not null
assertThrows(Exception.class, lambda)   // lambda throws the given exception
```

---

## Project Layout

```
src/
├── main/java/com/examples/hello/
│   └── Calculator.java       ← the class being tested (the "subject under test")
└── test/java/com/examples/hello/
    └── CalculatorTest.java   ← the tests (mirrors the package of the class under test)
```

Test classes live in `src/test/` so they are never included in the final JAR.

---

## Commands

```bash
mvn test          # compile and run all tests
mvn test -pl .    # same, from any parent directory
```
