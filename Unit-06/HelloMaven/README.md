# HelloMaven

The smallest possible Maven project. The source code is trivial — the point is
everything around it.

---

## What is Maven?

Maven is a **build tool and dependency manager** for Java projects. It provides:

- A **standard directory layout** so every Maven project looks the same
- A **build lifecycle** (`compile → test → package → install → deploy`)
- A **dependency system** — declare a library once in `pom.xml` and Maven downloads it

---

## Standard Directory Layout

```
hello-maven/
├── pom.xml                          ← Project Object Model — the build definition
└── src/
    ├── main/
    │   └── java/                    ← production source code
    │       └── com/examples/hello/
    │           └── Main.java
    └── test/
        └── java/                    ← test source code (empty here)
```

Maven enforces this layout so that every plugin knows where to find things
without additional configuration.

---

## The POM (`pom.xml`)

The POM is the heart of a Maven project. At minimum it declares:

| Element        | Purpose                                                  |
|----------------|----------------------------------------------------------|
| `groupId`      | Organisation identifier (like a reversed domain name)   |
| `artifactId`   | Project name                                             |
| `version`      | Current version (`SNAPSHOT` = in development)           |
| `packaging`    | Output type (`jar`, `war`, `pom`, …)                    |
| `properties`   | Key/value pairs reused throughout the POM                |
| `dependencies` | External libraries this project needs                    |
| `build/plugins`| Tools that participate in the build lifecycle            |

---

## Commands

```bash
mvn compile          # compile src/main/java → target/classes/
mvn exec:java        # compile and run Main
mvn package          # compile + test + produce target/hello-maven-1.0-SNAPSHOT.jar
mvn clean            # delete the target/ directory
mvn clean package    # clean, then package from scratch
```
