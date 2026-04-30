# 25 — Dependency Injection: The User Registration Service

## The Story

Your user registration service needs to send a welcome email. The obvious first
draft hard-codes the email sender:

```java
public class UserRegistrationService {
    private final SmtpMailSender mailer = new SmtpMailSender("mail.example.com", 587);
    ...
}
```

Now your unit tests fire off real emails every time they run. Your CI pipeline
needs outbound SMTP access. Your staging environment either sends to real
addresses or silently drops mail. Changing to a cloud API means editing the
service class, not just the configuration.

The fix: the service shouldn't *create* its dependencies. It should *receive*
them.

```java
public class UserRegistrationService {
    private final MailSender mailer;          // an interface, not a class
    public UserRegistrationService(MailSender mailer) { this.mailer = mailer; }
}
```

Production code injects `SmtpMailSender`. Tests inject `InMemoryMailSender`.
The service stays unchanged either way.

---

## The Problem It Solves

Hard-coded dependencies couple a class to a specific implementation, making it:

- Untestable without a real environment
- Inflexible when requirements change (new provider, new protocol)
- Impossible to run in parallel environments with different config

Dependency Injection inverts control: the *caller* decides what implementation
to supply. The class declares what it *needs* (an interface), not what it *uses*
(a class).

---

## Three Injection Styles

| Style | Shown here | Trade-off |
|-------|-----------|-----------|
| Constructor injection | ✓ | Preferred — dependencies are explicit and immutable |
| Setter injection | ✗ | Allows optional dependencies; risk of partial configuration |
| Field injection (annotation) | ✗ | Concise but hides dependencies and complicates testing |

---

## Structure

```
MailSender              ← Interface (the dependency)
  ├── SmtpMailSender    ← Production implementation
  └── InMemoryMailSender← Test double / dev implementation

UserRegistrationService ← Client (receives MailSender via constructor)
Main                    ← Composition root (wires implementations to clients)
```

---

## When to Use It

| Situation | Example |
|-----------|---------|
| A class needs an external resource | Database, email, HTTP client, clock |
| You want to swap implementations | Real vs mock, prod vs dev |
| Testing without infrastructure | Unit tests, CI without external services |

Spring, Guice, CDI, and Dagger are all DI *containers* — they automate the
constructor wiring you see in `Main.java`.

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```
