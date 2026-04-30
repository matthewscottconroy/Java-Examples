# 02 — Recursive Descent Parser

A hand-written LL(1) parser for arithmetic expressions. Each rule of the grammar maps directly to one Java method — the call stack mirrors the parse tree.

## Grammar

```
expr   ::= term   ( ('+' | '-') term   )*
term   ::= factor ( ('*' | '/') factor )*
factor ::= '(' expr ')' | '-' factor | NUMBER
```

The precedence rules (`*` binds tighter than `+`) fall out naturally from the grammar structure.

## Key files

| File | Purpose |
|------|---------|
| `Expr.java` | Sealed AST: `Num(double)`, `Neg(Expr)`, `BinOp(char, Expr, Expr)` |
| `Parser.java` | `expr()`, `term()`, `factor()` methods; `ParseException` on bad input |
| `Main.java` | Parses and evaluates several expressions, pretty-prints the AST |

## Key Java features

| Feature | Where |
|---------|-------|
| `sealed interface` + `record` deconstruction | `Expr.java` |
| `switch` with `when` guards | `eval()` — char values can't be constant sub-patterns in Java 21 record patterns |
| `StringCharacterIterator` for single-char lookahead | `Parser.java` |

## Run

```bash
mvn exec:java
mvn test
```
