package com.example.demo.enums;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents the supported categories for services/components.
 */
public enum Category {

    /**
     * Databases, caches, storage systems, queues, API gateways,
     * service discovery, load balancers, and other infrastructure components.
     *
     */
    INFRASTRUCTURE("infrastructure"),

    /**
     * Authentication, authorization, secret management,
     * identity and security-related tools.
     *
     */
    SECURITY_AND_IDENTITY("security_and_identity"),

    /**
     * Logging, monitoring, metrics, tracing, and other
     * observability-related tools.
     *
     */
    OBSERVABILITY("observability"),

    /**
     * Data processing, ETL, search, analytics, machine learning
     * models, and data pipelines.
     *
     * <p>Value: {@code "data_processing"}
     */
    DATA_PROCESSING("data_processing"),

    /**
     * General-purpose utilities such as email, SMS, push notifications,
     * payments, internal tools, and other user-facing utilities.
     *
     */
    UTILITY("utility"),

    /**
     * User-facing applications and service-facing applications.
     *
     */
    APPLICATION("application");
        
    private static final Set<String> VALID_VALUES = new HashSet<>();
    private final String value;

    static {
        for (Category category : Category.values()) {
            VALID_VALUES.add(category.getValue());
        }
    }
    
    Category(String value) {
        this.value = value;
    }

    /**
     * Returns the enum constant name.
     *
     * <p>For example:
     * <pre>
     * Category.APPLICATION.getName();
     * // Returns: "APPLICATION"
     * </pre>
     *
     * @return enum constant name
     */
    public String getName() {
        return name();
    }

    /**
     * Returns the external string value of this category.
     *
     * <p>For example:
     * <pre>
     * Category.APPLICATION.getValue();
     * // Returns: "application"
     * </pre>
     *
     * @return external category value
     */
    public String getValue() {
        return value;
    }

    /**
     * Checks whether the provided string is a valid category value.
     *
     * <p>The comparison is case-insensitive.
     *
     * <p>Examples:
     * <pre>
     * Category.isValid("application");  // true
     * Category.isValid("APPLICATION");  // true
     * Category.isValid("Application");  // true
     * Category.isValid("unknown");      // false
     * Category.isValid(null);            // false
     * </pre>
     *
     * @param input category value to validate
     * @return {@code true} if the value is valid; otherwise {@code false}
     */
    public static boolean isValid(String input) {
        if (input == null) {
            return false;
        }

        return VALID_VALUES.contains(input.toLowerCase());
    }
}