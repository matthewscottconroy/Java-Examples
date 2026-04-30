package com.patterns.proxy;

import java.util.Optional;

/**
 * Subject interface — both the real database and the caching proxy implement this.
 *
 * <p>Client code depends only on this interface; it never knows whether it is
 * talking to the real database or to a proxy that caches results.
 */
public interface ProductRepository {

    /**
     * Finds a product by its SKU.
     *
     * @param sku the product identifier
     * @return the product, or empty if not found
     */
    Optional<String> findBySku(String sku);

    /**
     * Returns the total number of products in the catalogue.
     *
     * @return product count
     */
    int count();
}
