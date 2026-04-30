package com.functional.result;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses CSV rows into domain objects, returning {@link Result} instead of
 * throwing exceptions.
 *
 * <p>Errors propagate as values. The caller decides whether to handle them,
 * collect them, or fail fast — without try/catch scattered through the pipeline.
 */
public final class CsvParser {

    private CsvParser() {}

    /**
     * Parse a single CSV row into a {@link Product}.
     *
     * <p>Each parsing step returns a {@code Result} and composes with
     * {@code flatMap} — failures short-circuit the chain automatically.
     *
     * @param row raw CSV line: "id,name,price,stock"
     * @return {@code Ok(Product)} on success, {@code Err(message)} on any failure
     */
    public static Result<Product> parseRow(String row) {
        if (row == null || row.isBlank()) return Result.err("Empty row");

        String[] parts = row.split(",", -1);
        if (parts.length < 4) return Result.err("Expected 4 fields, got " + parts.length);

        return parseId(parts[0].trim())
                .flatMap(id -> parseName(parts[1].trim())
                .flatMap(name -> parsePrice(parts[2].trim())
                .flatMap(price -> parseStock(parts[3].trim())
                .map(stock -> new Product(id, name, price, stock)))));
    }

    /** Parse and validate all rows; collect both successes and failures. */
    public static ParseSummary parseAll(List<String> rows) {
        List<Product> products = new ArrayList<>();
        List<String>  errors   = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            Result<Product> r = parseRow(rows.get(i));
            if (r.isOk()) {
                products.add(r.getOrThrow());
            } else {
                errors.add("Row " + (i + 1) + ": " + r.errorMessage());
            }
        }
        return new ParseSummary(products, errors);
    }

    private static Result<Integer> parseId(String s) {
        try {
            int id = Integer.parseInt(s);
            return id > 0 ? Result.ok(id) : Result.err("ID must be positive, got " + id);
        } catch (NumberFormatException e) {
            return Result.err("Invalid ID: '" + s + "'");
        }
    }

    private static Result<String> parseName(String s) {
        return s.isBlank() ? Result.err("Name must not be blank") : Result.ok(s);
    }

    private static Result<Double> parsePrice(String s) {
        try {
            double price = Double.parseDouble(s);
            return price >= 0 ? Result.ok(price) : Result.err("Price must be non-negative, got " + price);
        } catch (NumberFormatException e) {
            return Result.err("Invalid price: '" + s + "'");
        }
    }

    private static Result<Integer> parseStock(String s) {
        try {
            int stock = Integer.parseInt(s);
            return stock >= 0 ? Result.ok(stock) : Result.err("Stock must be non-negative, got " + stock);
        } catch (NumberFormatException e) {
            return Result.err("Invalid stock: '" + s + "'");
        }
    }
}
