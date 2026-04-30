# 12 — Optional: The User Profile Lookup

## The Story

You look up a user by ID. Sometimes they exist; sometimes they don't. In the
pre-Optional world, the method returns null for "not found," and every caller
has to remember to check:

```java
UserProfile user = repo.findById(id);
if (user != null) {
    sendEmail(user.email()); // but email might also be null...
}
```

Miss one check and the program crashes at runtime. The null is a silent lie in
the type signature — `UserProfile findById(String id)` claims to always return
a profile, but doesn't.

With Optional, the type is honest:

```java
Optional<UserProfile> user = repo.findById(id);
user.ifPresent(u -> sendEmail(u.email()));
```

The caller must confront the possibility of absence before accessing the value.

---

## The Optional API

**Wrapping values:**
```java
Optional.of(value)          // fails fast if value is null
Optional.ofNullable(value)  // empty if null, present if not
Optional.empty()            // an empty Optional
```

**Transforming (without unwrapping):**
```java
opt.map(User::email)                    // apply function, stay wrapped
opt.flatMap(u -> repo.findPrimary(u))   // function returns Optional — flatten
opt.filter(u -> u.tier().equals("pro")) // keep if predicate holds
```

**Unwrapping:**
```java
opt.orElse("default")                // value or fallback
opt.orElseGet(() -> expensive())     // value or lazy fallback
opt.orElseThrow(() -> new Ex(...))   // value or exception
opt.get()                            // value — throws if empty (avoid this)
```

**Side-effects:**
```java
opt.ifPresent(u -> log(u))           // action only if present
opt.isPresent()                      // boolean check
opt.isEmpty()                        // boolean check (Java 11+)
```

---

## When *Not* to Use Optional

Optional is for *return values* that may be absent. It is not for:

- Method parameters — just use an overload or null-check at the boundary
- Fields — adds allocation on every object; use a sentinel value instead
- Collections — `List.of()` is already an empty collection

---

## Commands

```bash
mvn compile exec:java
mvn test
```
