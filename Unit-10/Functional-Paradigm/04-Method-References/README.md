# 04 — Method References: The Contact Book

## The Story

You're sorting a contact list by last name. You write:

```java
contacts.sort((a, b) -> a.lastName().compareTo(b.lastName()));
```

That lambda does nothing except delegate to an existing method. Java lets you
drop the delegation and just name the method:

```java
contacts.sort(Comparator.comparing(Contact::lastName));
```

`Contact::lastName` *is* the function. There is no wrapper.

---

## The Four Kinds

| Kind | Syntax | Lambda equivalent |
|------|--------|-------------------|
| Static method | `Contact::parse` | `s -> Contact.parse(s)` |
| Unbound instance | `Contact::fullName` | `c -> c.fullName()` |
| Bound instance | `System.out::println` | `s -> System.out.println(s)` |
| Constructor | `Contact::new` | `(...) -> new Contact(...)` |

**Static** — the method belongs to the class, not an instance. Use when
converting or constructing from another type.

**Unbound instance** — the method is called on whatever object arrives as the
first argument. The most common kind in stream pipelines.

**Bound instance** — the receiver is already fixed. `System.out::println` always
calls `println` on *that specific* `PrintStream`.

**Constructor** — creates a new instance. Common with factory interfaces like
`Supplier<T>` or `Function<String, Contact>`.

---

## When to Use Them

Prefer a method reference over a lambda when the lambda body is nothing but a
call to an existing method. It's shorter, it gives the reader an immediate name
to look up, and it avoids the intermediate parameter variable.

Keep a lambda when the body contains logic — even one character of arithmetic or
a ternary expression — because then the lambda is expressing something new, not
just naming something existing.

---

## Commands

```bash
mvn compile exec:java
mvn test
```
