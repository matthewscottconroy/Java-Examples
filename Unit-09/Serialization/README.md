# Java Serialization — Graduated Examples

Three self-contained Maven projects covering Java's built-in serialization mechanism,
its pitfalls, and modern alternatives.

| # | Directory | Topics covered |
|---|-----------|----------------|
| 1 | `01-BasicSerialization` | Serializable, ObjectStream, serialVersionUID, transient, static fields |
| 2 | `02-AdvancedSerialization` | writeObject/readObject, Externalizable, writeReplace/readResolve, inheritance |
| 3 | `03-SecurityAndAlternatives` | Deserialization vulnerabilities, ObjectInputFilter, modern alternatives |

## Running any example

```
cd 01-BasicSerialization
mvn compile exec:java
```

## The mental model

```
01  basic               Serializable marker → JVM handles everything automatically
                        serialVersionUID guards version compatibility
                        transient excludes sensitive / non-serializable fields

02  advanced            writeObject/readObject for custom logic (encrypt, compress)
                        Externalizable for full manual control
                        writeReplace/readResolve for canonical instances (Singleton, Enum)

03  security            Untrusted data + readObject() = arbitrary code execution risk
                        ObjectInputFilter: allowlist classes before any object is built
                        Modern path: records + Jackson/Gson/Protobuf instead of Serializable
```

## Important note on security

Java's built-in serialization should be treated as a **legacy mechanism**.
Never deserialize data from untrusted sources without an ObjectInputFilter allowlist.
For new designs, prefer an external format (JSON, Protobuf) with an explicit schema.
