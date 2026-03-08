# HelloLibrary

A reusable code library with **no `main` method**.  It is meant to be compiled
once and consumed as a `<dependency>` by other Maven projects.

---

## What is a Library?

A library (or "library JAR") is a collection of reusable classes packaged for
distribution.  Unlike an application:

| Application                     | Library                          |
|---------------------------------|----------------------------------|
| Has a `main` method             | No `main` method                 |
| Launched with `java -jar`       | Added to another project's classpath |
| Produces a runnable program     | Produces a reusable artifact     |
| Uses `exec-maven-plugin`        | No `exec-maven-plugin`           |

Libraries are still tested — they just have no entry point.

---

## Building and Installing

```bash
mvn test      # run the unit tests
mvn install   # compile, test, and install to the local Maven repository (~/.m2/)
```

After `mvn install`, any Maven project on this machine can declare this library
as a dependency.

---

## Using This Library as a Dependency

In another project's `pom.xml`, add:

```xml
<dependency>
    <groupId>com.examples</groupId>
    <artifactId>hello-library</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

Then import and use the classes normally:

```java
import com.examples.hello.StringUtils;

String result = StringUtils.repeat("ha", 3);   // "hahaha"
boolean ok    = StringUtils.isPalindrome("racecar"); // true
```

---

## Commands

```bash
mvn test       # run unit tests
mvn install    # install to local repo so other projects can depend on it
mvn package    # build the JAR without installing (output: target/hello-library-1.0-SNAPSHOT.jar)
```
