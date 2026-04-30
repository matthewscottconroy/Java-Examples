# 06 — Dynamic Proxy

`java.lang.reflect.Proxy` creates an object at runtime that implements any interface. Every method call on that object is routed through an `InvocationHandler`, which can wrap the call with arbitrary behaviour.

This is the runtime equivalent of the Decorator pattern — without writing a separate class per combination of concerns.

## Proxy factories

| Factory method | Behaviour added |
|----------------|-----------------|
| `ServiceProxy.logging(target, iface, log)` | Appends `"CALL method(args) → result"` or `"ERROR ..."` to a list |
| `ServiceProxy.timing(target, iface, timings)` | Accumulates nanoseconds per method name in a map |
| `ServiceProxy.caching(target, iface)` | Returns cached result on repeated identical calls |
| `ServiceProxy.retrying(target, iface, maxAttempts)` | Retries up to `maxAttempts` times on `RuntimeException` |

All four factories are generic over `<T>` and work with any interface.

## InvocationTargetException

When `method.invoke(target, args)` throws, the exception is wrapped in `InvocationTargetException`. Proxy handlers always unwrap it with `e.getCause()` before re-throwing, so callers see the original exception type.

## Run

```bash
mvn exec:java   # shows all four proxies wrapping a Calculator and a FlakyService
mvn test
```
