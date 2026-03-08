# HelloJavadocs

Demonstrates how to write Javadoc comments and generate HTML documentation.

---

## What is Javadoc?

Javadoc is a tool bundled with the JDK that reads specially formatted `/** … */`
comments from Java source files and produces an HTML website documenting the
public API. Every class in the Java standard library is documented this way.

---

## Comment Syntax

```java
/**
 * One-sentence summary of what this class or method does.
 *
 * <p>Further paragraphs are wrapped in an HTML <p> tag.
 *
 * @param  name   describes a method parameter
 * @return        describes the return value
 * @throws SomeEx describes when this exception is thrown
 * @author        your name
 * @version       1.0
 * @since         1.0
 * @see           OtherClass#otherMethod()
 */
```

Common inline tags:
- `{@code someCode}` — renders as monospace
- `{@link ClassName#method()}` — creates a hyperlink to another type
- `{@literal <text>}` — treats HTML characters as plain text

---

## Commands

```bash
mvn exec:java          # run the program
mvn javadoc:javadoc    # generate HTML docs → target/site/apidocs/index.html
```

Open `target/site/apidocs/index.html` in a browser to see the generated documentation.
