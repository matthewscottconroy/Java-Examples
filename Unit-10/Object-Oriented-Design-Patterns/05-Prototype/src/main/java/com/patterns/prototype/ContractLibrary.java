package com.patterns.prototype;

import java.util.HashMap;
import java.util.Map;

/**
 * Prototype registry — stores master contract templates by name.
 *
 * <p>The library holds the canonical, fully-populated templates. Callers ask
 * for a clone of a named template; the library never hands out the master.
 *
 * <p>This is the optional "prototype manager" role in the Prototype pattern:
 * a registry that stores and retrieves prototypes by key.
 */
public class ContractLibrary {

    private final Map<String, Contract> templates = new HashMap<>();

    /**
     * Registers a master template under the given key.
     *
     * @param key      the template name used to retrieve it later
     * @param template the master contract (should not be modified after registration)
     */
    public void register(String key, Contract template) {
        templates.put(key, template);
    }

    /**
     * Returns a clone of the named template, ready to be customised.
     *
     * <p>The master template is never returned directly; every call gets a
     * fresh copy. This is the core of the Prototype pattern.
     *
     * @param key the name of the template to clone
     * @return an independent copy of the template
     * @throws IllegalArgumentException if no template with this key exists
     */
    public Contract get(String key) {
        Contract template = templates.get(key);
        if (template == null) {
            throw new IllegalArgumentException("No template registered for key: " + key);
        }
        return template.clone();
    }
}
