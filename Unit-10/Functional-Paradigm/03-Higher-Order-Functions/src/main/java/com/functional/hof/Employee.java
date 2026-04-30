package com.functional.hof;

/**
 * Immutable employee record.
 *
 * @param id         unique identifier
 * @param name       full name
 * @param department department name
 * @param salaryUsd  annual salary in US dollars
 * @param fullTime   true if employed full-time
 */
public record Employee(int id, String name, String department, double salaryUsd, boolean fullTime) {}
