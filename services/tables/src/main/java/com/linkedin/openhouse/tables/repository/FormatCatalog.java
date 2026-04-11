package com.linkedin.openhouse.tables.repository;

/**
 * Marker interface for format-specific catalog implementations. Each table format (Iceberg, Lance)
 * provides its own independent repository/service stack. This interface exists to enable shared
 * typing where needed in the future.
 */
public interface FormatCatalog {}
