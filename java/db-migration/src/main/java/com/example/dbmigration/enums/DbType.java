package com.example.dbmigration.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the supported database types.
 */
public enum DbType {

	/**
	 * MongoDB database.
	 */
	MONGO("mongo"),

	/**
	 * PostgreSQL database.
	 */
	POSTGRES("postgres"),

	/**
	 * H2 in-memory or file-based database.
	 */
	H2("h2");

	private static final Map<String, DbType> VALUE_TO_TYPE = new HashMap<>();
	private final String value;

	static {
		for (DbType dbType : DbType.values()) {
			VALUE_TO_TYPE.put(dbType.getValue(), dbType);
		}
	}
	
	DbType(String value) {
		this.value = value;
	}

	/**
	 * Returns the enum constant name.
	 *
	 * <p>For example:
	 * <pre>
	 * DbType.POSTGRES.getName();
	 * // Returns: "POSTGRES"
	 * </pre>
	 *
	 * @return enum constant name
	 */
	public String getName() {
		return name();
	}

	/**
	 * Returns the string value associated with this database type.
	 *
	 * <p>For example:
	 * <pre>
	 * DbType.POSTGRES.getValue();
	 * // Returns: "postgres"
	 * </pre>
	 *
	 * @return database type value
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Returns the database type corresponding to the specified value.
	 *
	 * <p>The comparison is case-insensitive.
	 *
	 * <p>Examples:
	 * <pre>
	 * DbType.fromValue("postgres");  // POSTGRES
	 * DbType.fromValue("POSTGRES");  // POSTGRES
	 * DbType.fromValue("h2");        // H2
	 * DbType.fromValue("unknown");   // null
	 * DbType.fromValue(null);        // null
	 * </pre>
	 *
	 * @param value database type value
	 * @return corresponding {@link DbType}, or {@code null} if not found
	 */
	public static DbType fromValue(String value) {
		if (value == null) {
			return null;
		}

		return VALUE_TO_TYPE.get(value.toLowerCase());
	}
	
	/**
	 * Checks whether the specified value represents a valid database type.
	 *
	 * <p>The comparison is case-insensitive.
	 *
	 * @param value database type value to validate
	 * @return {@code true} if the value is a supported database type;
	 *         otherwise {@code false}
	 */
	public static boolean isValid(String value) {
		return fromValue(value) != null;
	}
}