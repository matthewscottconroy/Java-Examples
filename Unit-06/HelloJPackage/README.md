# HelloJPackage

Demonstrates `jpackage`: the JDK tool that wraps a Java application into a
native, self-contained executable — no JRE installation required on the target machine.

---

## What is jpackage?

`jpackage` (included with JDK 14+) takes a JAR file and a JDK, and produces a
platform-native distributable:

| Platform | Output types                      |
|----------|-----------------------------------|
| macOS    | `.dmg`, `.pkg`, `app-image`       |
| Linux    | `.deb`, `.rpm`, `app-image`       |
| Windows  | `.msi`, `.exe`, `app-image`       |

`app-image` creates a self-contained directory with an executable launcher and
a bundled JRE.  It works on all platforms and does not require admin rights.

---

## Two-Step Process

### Step 1 — Build the executable JAR

```bash
mvn package
```

This produces `target/hello-jpackage-1.0-SNAPSHOT.jar` with a `Main-Class`
entry in its manifest.

### Step 2 — Bundle with jpackage

```bash
jpackage \
  --input   target \
  --name    HelloJPackage \
  --main-jar hello-jpackage-1.0-SNAPSHOT.jar \
  --main-class com.examples.hello.Main \
  --type    app-image \
  --dest    target/dist
```

The result is a `target/dist/HelloJPackage/` directory.  Launch the app:

```bash
# macOS / Linux
target/dist/HelloJPackage/bin/HelloJPackage

# Windows
target\dist\HelloJPackage\HelloJPackage.exe
```

---

## Notes

- `jpackage` must be run on the target platform (cross-compilation is not supported).
- For a `.dmg` or `.deb` installer, change `--type app-image` to `--type dmg`
  or `--type deb`.  Installer types may require platform-specific tools
  (e.g. WiX Toolset on Windows for `.msi`).
- The bundled JRE makes the output large (~50 MB) but means the user does not
  need Java installed.
