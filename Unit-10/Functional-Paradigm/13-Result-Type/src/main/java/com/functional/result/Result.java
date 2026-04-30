package com.functional.result;

import java.util.function.Function;

/**
 * A typed result that is either a success carrying a value, or a failure
 * carrying an error — never both, never neither.
 *
 * <p>This is Java's approximation of the {@code Either} type found in Haskell,
 * Scala, and Rust's {@code Result<T, E>}. Unlike checked exceptions, errors
 * flow as values through the call chain and compose cleanly with functional
 * pipelines.
 *
 * @param <T> the success value type
 */
public sealed interface Result<T> permits Result.Success, Result.Failure {

    /** Construct a successful result. */
    static <T> Result<T> ok(T value) {
        return new Success<>(value);
    }

    /** Construct a failed result. */
    static <T> Result<T> err(String message) {
        return new Failure<>(message);
    }

    boolean isOk();
    boolean isErr();

    /**
     * Transform the success value, propagating any failure unchanged.
     *
     * <p>This is the monadic {@code map} operation: it lets you chain
     * transformations without unwrapping and re-wrapping at every step.
     */
    <U> Result<U> map(Function<T, U> mapper);

    /**
     * Transform the success value with a function that may itself fail.
     *
     * <p>This is the monadic {@code flatMap} / {@code bind} operation: it
     * prevents nesting ({@code Result<Result<U>>}) when the next step also
     * returns a {@code Result}.
     */
    <U> Result<U> flatMap(Function<T, Result<U>> mapper);

    /** Return the success value, or throw if this is a failure. */
    T getOrThrow();

    /** Return the success value, or the given default if this is a failure. */
    T getOrElse(T fallback);

    /** Return the error message, or throw if this is a success. */
    String errorMessage();

    // ── Record implementations ───────────────────────────────────────────

    record Success<T>(T value) implements Result<T> {
        public boolean isOk()  { return true;  }
        public boolean isErr() { return false; }

        public <U> Result<U> map(Function<T, U> mapper) {
            try { return Result.ok(mapper.apply(value)); }
            catch (Exception e) { return Result.err(e.getMessage()); }
        }
        public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
            try { return mapper.apply(value); }
            catch (Exception e) { return Result.err(e.getMessage()); }
        }
        public T      getOrThrow()         { return value; }
        public T      getOrElse(T fallback){ return value; }
        public String errorMessage()       { throw new UnsupportedOperationException("Success has no error"); }
        public String toString()           { return "Ok(" + value + ")"; }
    }

    record Failure<T>(String message) implements Result<T> {
        public boolean isOk()  { return false; }
        public boolean isErr() { return true;  }

        @SuppressWarnings("unchecked")
        public <U> Result<U> map(Function<T, U> mapper)           { return (Result<U>) this; }
        @SuppressWarnings("unchecked")
        public <U> Result<U> flatMap(Function<T, Result<U>> mapper){ return (Result<U>) this; }
        public T      getOrThrow()          { throw new RuntimeException(message); }
        public T      getOrElse(T fallback) { return fallback; }
        public String errorMessage()        { return message; }
        public String toString()            { return "Err(" + message + ")"; }
    }
}
