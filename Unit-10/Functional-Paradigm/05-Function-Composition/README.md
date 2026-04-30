# 05 — Function Composition: The Validation Pipeline

## The Story

User registration requires a username that's 3–20 characters, alphanumeric with
underscores only. A password that's at least 8 characters, has an uppercase
letter, and has a digit. An email that matches the standard format.

You could write one enormous `validate(username, password, email)` method. Or
you could write small, single-responsibility validators and *compose* them into
a pipeline.

```java
Function<String, ValidationResult> usernameValidator = Validator.chain(
    Validator.notBlank("username"),
    Validator.minLength("username", 3),
    Validator.maxLength("username", 20),
    Validator.matches("username", "[a-zA-Z0-9_]+", "invalid chars")
);
```

Each line is a function. `chain` connects them so the output of each step feeds
the next. The pipeline short-circuits on the first failure.

---

## Function Composition

**`Function.andThen`** — apply f, then apply g to the result:
```java
Function<String, String> trim      = String::trim;
Function<String, String> uppercase = String::toUpperCase;
Function<String, String> prepare   = trim.andThen(uppercase);
// prepare.apply("  hello  ") → "HELLO"
```

**`Function.compose`** — apply g first, then f (reverse order):
```java
Function<String, String> prepare = uppercase.compose(trim);
// same result, different reading direction
```

These methods let you build complex transformations from single-purpose parts.
The parts stay small and testable in isolation. The composition is readable —
it describes what happens in order.

---

## The Short-Circuit Pattern

Standard `andThen` always executes both functions regardless of the intermediate
result. The custom `Validator.chain` introduces short-circuiting: if a step
returns a failure, subsequent steps are skipped. This is the same logic as
`&&` in a boolean expression, applied to functions.

---

## Why This Matters

Small, composed functions are:

- **Individually testable** — each validator has a test, not the composed chain
- **Reusable** — `notBlank` is used for username, password, and email
- **Readable** — the chain reads like a requirement specification
- **Maintainable** — adding a rule is adding one line to the chain

---

## Commands

```bash
mvn compile exec:java
mvn test
```
