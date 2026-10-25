package com.example.demo.config;

public class Constants {

    private Constants() {
    }

    public static final int DEFAULT_PORT = 6000;
    public static final String ENTITY = "port_registry";
    public static final String MAX_PORT = "maxPort";

    /**
     * Entity columns
     */
    public static final String NAME_COL = "name";
    public static final String CATEGORY_COL = "category";
    public static final String PRIVATE_IP_COL = "private_ip";
    public static final String PORT_COL = "port";
    public static final String DESCRIPTION_COL = "description";
    public static final String NETWORK_SCOPE_COL = "network_scope";
    public static final String CREATED_AT_COL = "created_at";
    public static final String UPDATED_AT_COL = "updated_at";
}