/**
 * Application constants for default pagination and sorting values.
 */
package com.accessiq.util;

/**
 * Utility class containing application-wide constants.
 */
public final class AppConstants {

    /** Default page number for pagination (0-indexed). */
    public static final String DEFAULT_PAGE_NUMBER = "0";

    /** Default page size for pagination. */
    public static final String DEFAULT_PAGE_SIZE = "20";

    /** Default sorting field for pagination. */
    public static final String DEFAULT_SORT_BY = "id";

    /** Default sorting direction. */
    public static final String DEFAULT_SORT_DIR = "asc";

    /** Maximum allowed page size. */
    public static final int MAX_PAGE_SIZE = 1000;

    /** Private constructor to prevent instantiation. */
    private AppConstants() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}