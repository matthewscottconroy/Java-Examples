package com.meta.reflect;

/**
 * Marker interface for pluggable report generators.
 * The plugin system discovers implementations at runtime via reflection.
 */
public interface ReportPlugin {
    String name();
    String generate(String data);
}
