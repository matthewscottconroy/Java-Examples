# 12 — Proxy: The Caching Database Proxy

## The Story

Your product catalogue page loads slowly. Every visitor triggers a SQL query:
`SELECT * FROM products WHERE sku='SKU-001'`. The database server is 50ms away,
and you're getting 500 requests per second. Nearly all of them ask for the same
20 products.

A **Caching Proxy** sits in front of the database. The first request for each SKU
is forwarded to the database and the result is stored. Every subsequent request for
that same SKU is served from memory in microseconds — the database never knows.

The key insight: the proxy and the real database implement the **same interface**.
The rest of the application never changes.

---

## The Problem It Solves

You want to control access to an object — to add caching, logging, access control,
lazy initialisation, or remote access — **without changing the client code or the
real object's code**.

The proxy stands between the client and the real subject, implementing the same
interface so the client can't tell the difference.

**Other common proxy uses:**
- **Virtual proxy** — delays expensive object creation until first use
- **Protection proxy** — checks permissions before forwarding calls
- **Remote proxy** — hides that the subject lives on another machine (RMI, gRPC stubs)

---

## Structure

```
ProductRepository              ← Subject interface
  ├── DatabaseProductRepository ← Real Subject (expensive, slow)
  └── CachingProductProxy       ← Proxy (cheap cache in front)
```

---

## When to Use It

| Situation | Example |
|-----------|---------|
| Cache results of expensive operations | Database query cache, HTTP response cache |
| Control access with permissions | Security proxy, firewall |
| Delay expensive initialisation | Virtual proxy for a large image |
| Add logging or metrics transparently | Logging proxy around any service |

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```
