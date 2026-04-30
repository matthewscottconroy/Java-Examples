package com.functional.result;

import java.util.List;

/**
 * Summary of a batch parse: successfully parsed products and error messages.
 *
 * @param products successfully parsed records
 * @param errors   error descriptions for failed rows (row number + message)
 */
public record ParseSummary(List<Product> products, List<String> errors) {
    public int successCount() { return products.size(); }
    public int errorCount()   { return errors.size(); }
}
