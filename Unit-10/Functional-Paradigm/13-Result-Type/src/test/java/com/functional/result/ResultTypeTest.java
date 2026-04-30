package com.functional.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResultTypeTest {

    @Test
    @DisplayName("Valid CSV row parses to Ok(Product)")
    void validRowParsesOk() {
        Result<Product> r = CsvParser.parseRow("1,Laptop,999.99,10");
        assertTrue(r.isOk());
        assertEquals("Laptop", r.getOrThrow().name());
    }

    @Test
    @DisplayName("Blank name produces Err")
    void blankNameIsErr() {
        Result<Product> r = CsvParser.parseRow("1,,999.99,10");
        assertTrue(r.isErr());
        assertTrue(r.errorMessage().contains("Name"));
    }

    @Test
    @DisplayName("Negative price produces Err")
    void negativePriceIsErr() {
        Result<Product> r = CsvParser.parseRow("1,Laptop,-10.00,5");
        assertTrue(r.isErr());
    }

    @Test
    @DisplayName("Non-numeric price produces Err")
    void nonNumericPriceIsErr() {
        Result<Product> r = CsvParser.parseRow("1,Laptop,abc,5");
        assertTrue(r.isErr());
    }

    @Test
    @DisplayName("map transforms Ok value")
    void mapTransformsOk() {
        Result<Double> r = CsvParser.parseRow("1,Laptop,1000.00,5")
                .map(p -> p.price() * 0.90);
        assertTrue(r.isOk());
        assertEquals(900.0, r.getOrThrow(), 0.001);
    }

    @Test
    @DisplayName("map on Err propagates the error")
    void mapPropagatesErr() {
        Result<Double> r = CsvParser.parseRow("bad,Laptop,1000.00,5")
                .map(p -> p.price());
        assertTrue(r.isErr());
    }

    @Test
    @DisplayName("flatMap chains two fallible operations")
    void flatMapChains() {
        Result<String> r = CsvParser.parseRow("1,Laptop,1000.00,5")
                .flatMap(p -> p.stock() > 0
                        ? Result.ok("In stock: " + p.name())
                        : Result.err("Out of stock"));
        assertTrue(r.isOk());
        assertTrue(r.getOrThrow().contains("Laptop"));
    }

    @Test
    @DisplayName("getOrElse returns fallback on Err")
    void getOrElseOnErr() {
        Product fallback = new Product(0, "Unknown", 0.0, 0);
        Product result = CsvParser.parseRow("bad,x,y,z").getOrElse(fallback);
        assertEquals(fallback, result);
    }

    @Test
    @DisplayName("parseAll separates successes and errors")
    void parseAllSeparates() {
        ParseSummary summary = CsvParser.parseAll(List.of(
                "1,Laptop,999.00,10",
                "bad,Chair,400.00,5",
                "3,Keyboard,89.99,20"
        ));
        assertEquals(2, summary.successCount());
        assertEquals(1, summary.errorCount());
    }
}
