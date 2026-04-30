package com.functional.collectors;

/**
 * Immutable order record.
 *
 * @param id        order identifier
 * @param customer  customer name
 * @param region    geographic region
 * @param product   product ordered
 * @param category  product category
 * @param total     order total in dollars
 * @param shipped   true if the order has been dispatched
 */
public record Order(int id, String customer, String region,
                    String product, String category, double total, boolean shipped) {}
