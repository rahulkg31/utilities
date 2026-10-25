package com.example.demo.enums;

import java.util.HashMap;
import java.util.Map;

import com.example.demo.config.Constants;

/**
 * Represents the supported fields of port registry.
 */
public enum Field {

	/**
	 * Unique identifier 
	 */
	ID("id"),

	/**
	 * Name of the service/component.
	 */
	NAME("name"),

	/**
	 * Category of the service/component.
	 */
	CATEGORY("category"),

	/**
	 * Private IP address of the service/component.
	 */
	PRIVATE_IP("privateIp"),

	/**
	 * Port on which the service/component is running.
	 */
	PORT("port"),

	/**
	 * Description of the service/component.
	 */
	DESCRIPTION("description"),

	/**
	 * Network scope associated with the service/component.
	 */
	NETWORK_SCOPE("networkScope"),

	/**
	 * Timestamp indicating when the service/component was created.
	 */
	CREATED_AT("createdAt"),

	/**
	 * Timestamp indicating when the service/component was last updated.
	 */
	UPDATED_AT("updatedAt");

	private static final Map<String, String> VALUE_TO_COLUMN = new HashMap<>();
	private final String val;

	static {
		VALUE_TO_COLUMN.put(NAME.getValue(), Constants.NAME_COL);
		VALUE_TO_COLUMN.put(CATEGORY.getValue(), Constants.CATEGORY_COL);
		VALUE_TO_COLUMN.put(PRIVATE_IP.getValue(), Constants.PRIVATE_IP_COL);
		VALUE_TO_COLUMN.put(PORT.getValue(), Constants.PORT_COL);
		VALUE_TO_COLUMN.put(NETWORK_SCOPE.getValue(), Constants.NETWORK_SCOPE_COL);
	}

	Field(String val) {
		this.val = val;
	}

	/**
	 * Returns the enum constant name.
	 *
	 * <p>For example:
	 * <pre>
	 * Field.PRIVATE_IP.getName();
	 * // Returns: "PRIVATE_IP"
	 * </pre>
	 *
	 * @return enum constant name
	 */
	public String getName() {
		return name();
	}

	/**
	 * Returns the string value associated with this field.
	 *
	 * <p>For example:
	 * <pre>
	 * Field.PRIVATE_IP.getValue();
	 * // Returns the configured value for PRIVATE_IP
	 * </pre>
	 *
	 * @return field value
	 */
	public String getValue() {
		return val;
	}

	/**
	 * Returns the database column name associated with the specified field value.
	 *
	 * <p>If the field value is not present in the mapping, {@code null} is
	 * returned.
	 *
	 * <p>For example:
	 * <pre>
	 * Field.getColumnFromValue("privateIp");
	 * // Returns the corresponding database column name
	 * </pre>
	 *
	 * @param value field value to look up
	 * @return corresponding database column name, or {@code null} if no mapping exists
	 */
	public static String getColumnFromValue(String value) {
		if (value == null) {
			return null;
		}

		return VALUE_TO_COLUMN.get(value);
	}
}