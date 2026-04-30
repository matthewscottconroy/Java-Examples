package com.functional.result;

/**
 * @param id    product ID (positive integer)
 * @param name  product name (non-blank)
 * @param price price in dollars (non-negative)
 * @param stock units in stock (non-negative)
 */
public record Product(int id, String name, double price, int stock) {}
