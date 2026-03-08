# HelloJar

Demonstrates how to build an executable JAR and run it with `java -jar`.

---

## What is a JAR?

A **JAR (Java Archive)** is a ZIP file containing:

- Compiled `.class` files
- Resources (images, config files, etc.)
- A `META-INF/MANIFEST.MF` metadata file

JAR files are the standard unit of distribution for Java code.

---

## Making a JAR Executable

By default a JAR is just an archive — the JVM does not know which class to
launch.  Adding a `Main-Class` entry to `MANIFEST.MF` fixes this:

```
Main-Class: com.examples.hello.Main
```

Maven's `maven-jar-plugin` writes this entry automatically when you configure:

```xml
<archive>
    <manifest>
        <mainClass>com.examples.hello.Main</mainClass>
    </manifest>
</archive>
```

---

## Commands

```bash
# Build the JAR
mvn package

# Run the JAR (no classpath or class name needed)
java -jar target/hello-jar-1.0-SNAPSHOT.jar
```

---

## Inspecting the Manifest

To see the manifest that Maven wrote inside the JAR:

```bash
unzip -p target/hello-jar-1.0-SNAPSHOT.jar META-INF/MANIFEST.MF
```
