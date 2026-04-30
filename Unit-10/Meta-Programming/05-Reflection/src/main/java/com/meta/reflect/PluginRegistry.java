package com.meta.reflect;

import java.util.*;

/**
 * A plugin registry that loads {@link ReportPlugin} implementations by class
 * name at runtime using reflection.
 *
 * <p>This pattern decouples the caller from concrete plugin classes. The only
 * compile-time dependency is the {@link ReportPlugin} interface. Concrete
 * implementations can be added, removed, or swapped without recompiling the
 * registry or its callers.
 */
public class PluginRegistry {

    private final Map<String, ReportPlugin> plugins = new LinkedHashMap<>();

    /**
     * Loads a plugin by its fully-qualified class name, instantiates it via
     * its no-arg constructor, and registers it.
     *
     * @throws ReflectiveOperationException if the class cannot be found,
     *         does not have a no-arg constructor, or does not implement
     *         {@link ReportPlugin}
     */
    public void register(String className) throws ReflectiveOperationException {
        Class<?> cls = Class.forName(className);
        if (!ReportPlugin.class.isAssignableFrom(cls)) {
            throw new IllegalArgumentException(className + " does not implement ReportPlugin");
        }
        ReportPlugin plugin = (ReportPlugin) cls.getDeclaredConstructor().newInstance();
        plugins.put(plugin.name(), plugin);
    }

    /** Registers an already-instantiated plugin directly. */
    public void register(ReportPlugin plugin) {
        plugins.put(plugin.name(), plugin);
    }

    public Optional<ReportPlugin> get(String name) {
        return Optional.ofNullable(plugins.get(name));
    }

    public Set<String> names() { return Collections.unmodifiableSet(plugins.keySet()); }

    public String run(String pluginName, String data) {
        return get(pluginName)
            .map(p -> p.generate(data))
            .orElseThrow(() -> new NoSuchElementException("Plugin not found: " + pluginName));
    }
}
