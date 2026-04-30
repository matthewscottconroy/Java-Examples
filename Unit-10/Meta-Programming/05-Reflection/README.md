# 05 — Reflection

`java.lang.reflect` lets a program examine and call its own structure at runtime — without knowing class names, method signatures, or field types at compile time.

## What it demonstrates

| Capability | API |
|-----------|-----|
| Inspect fields and methods | `getDeclaredFields()`, `getDeclaredMethods()` |
| Read a private field | `field.setAccessible(true)`, `field.get(obj)` |
| Invoke a method by name | `Class.getMethod(name, paramTypes)`, `method.invoke(obj, args)` |
| Instantiate by class name | `Class.forName(name)`, `clazz.getDeclaredConstructor().newInstance()` |
| Runtime plug-in loading | `PluginRegistry` validates the loaded class implements the interface |

## The plug-in registry

```
PluginRegistry registry = new PluginRegistry();
registry.register("com.meta.reflect.CsvPlugin");
registry.register("com.meta.reflect.JsonPlugin");
String output = registry.run("csv", "a=1;b=2");
```

The registry loads each class by string name, checks that it implements `ReportPlugin`, and stores it keyed by `plugin.name()`. No `instanceof` required — the type check is done reflectively via `isAssignableFrom`.

## Run

```bash
mvn exec:java   # loads CSV, JSON, and HTML plugins and generates sample reports
mvn test
```
