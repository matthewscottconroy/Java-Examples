# 07 — Runtime Annotations

Custom annotations are just metadata. With `@Retention(RetentionPolicy.RUNTIME)` they survive compilation and can be read reflectively at runtime to drive arbitrary behaviour — here, field-level validation.

## Annotations

| Annotation | Target | Effect |
|-----------|--------|--------|
| `@Required` | field | Fails if value is `null` or a blank `String` |
| `@Range(min, max)` | field | Fails if numeric value is outside `[min, max]` |
| `@Pattern(regex)` | field | Fails if `String` value doesn't match the regex |

All three carry a `message` attribute for the human-readable error.

## Validator

```java
ValidationResult r = Validator.validate(myObject);
if (!r.isValid()) r.violations().forEach(System.out::println);
```

`Validator.validate` iterates `getDeclaredFields()`, calls `setAccessible(true)` so private fields are readable, then checks each annotation present on the field.

## Annotated domain class

```java
class User {
    @Required(message = "Username must not be blank")
    String username;

    @Required
    @Pattern(regex = "^[\\w.+-]+@[\\w-]+\\.[a-z]{2,}$", message = "Must be a valid email")
    String email;

    @Range(min = 0, max = 150, message = "Age must be 0–150")
    int age;
}
```

## Run

```bash
mvn exec:java   # validates valid and invalid User / Product objects
mvn test
```
