# Java Reflection — Graduated Examples

Four self-contained Maven projects that build from reading class metadata to running a
custom annotation-driven validation framework.

| # | Directory | Topics covered |
|---|-----------|----------------|
| 1 | `01-ClassInspection` | Class<?> objects, hierarchy, fields, methods, constructors, modifiers |
| 2 | `02-DynamicInvocation` | newInstance, invoke, field get/set, private access via setAccessible |
| 3 | `03-Annotations` | Custom annotations, @Retention/@Target, runtime annotation scanning |
| 4 | `04-GenericsAtRuntime` | ParameterizedType, TypeVariable, WildcardType — generic metadata that survives erasure |

## Running any example

```
cd 01-ClassInspection
mvn compile exec:java
```

## Mental model

```
01  Class<?>            every object carries a Class object; it holds all compiler metadata
02  Method/Field        use them as handles to call methods and read/write fields at runtime
03  Annotation          structured compile-time metadata readable at runtime via @Retention(RUNTIME)
04  ParameterizedType   field/method/superclass signatures preserve generic info even after erasure
```
