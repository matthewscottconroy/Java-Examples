package com.functional.streams;

import java.time.LocalDate;

/**
 * Immutable record representing one sales transaction.
 *
 * @param id         unique transaction ID
 * @param product    product name
 * @param category   product category
 * @param region     sales region
 * @param salesperson name of the rep who made the sale
 * @param units      number of units sold
 * @param unitPrice  price per unit in dollars
 * @param date       date of the transaction
 */
public record Sale(int id, String product, String category, String region,
                   String salesperson, int units, double unitPrice, LocalDate date) {

    public double revenue() { return units * unitPrice; }
}
