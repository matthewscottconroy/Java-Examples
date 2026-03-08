# HelloPackages

Demonstrates how Java packages organise classes into named groups and how
`import` statements bring those classes into scope.

---

## What is a Package?

A package is a **named namespace** for a group of related classes.

- Prevents name collisions (two libraries can both have a `List` class as long
  as they are in different packages)
- Controls visibility (`package-private` members are only accessible within the
  same package)
- Maps directly to the directory structure on disk

---

## Package Naming Convention

Packages are written in all lowercase, typically as a reversed domain name:

```
com.examples.greeting
│    │       └── specific group within the project
│    └────────── project or organisation sub-name
└─────────────── top-level domain (reversed)
```

---

## Directory Layout

The directory structure **must** match the package name exactly:

```
src/main/java/
└── com/
    └── examples/
        ├── app/
        │   └── Main.java          (package com.examples.app)
        ├── greeting/
        │   └── Greeter.java       (package com.examples.greeting)
        └── farewell/
            └── Fareweller.java    (package com.examples.farewell)
```

---

## Import Statements

To use a class from another package, add an `import` at the top of the file:

```java
import com.examples.greeting.Greeter;    // import a specific class
import com.examples.farewell.*;          // import all classes in a package (avoid this)
```

Classes in `java.lang` (like `String`, `System`) are imported automatically.

---

## Commands

```bash
mvn exec:java    # compile and run Main
```
