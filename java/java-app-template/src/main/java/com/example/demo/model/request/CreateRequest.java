package com.example.demo.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class CreateRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "category is required")
    private String category;

    @NotBlank(message = "privateIp is required")
    @Pattern(
        regexp = "^((25[0-5]|2[0-4]\\d|[01]?\\d?\\d)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d?\\d)$",
        message = "privateIp must be a valid IPv4 address"
    )
    private String privateIp;

    private String description;

    @NotNull(message = "port is required")
    @Min(value = 1, message = "port must be >= 1")
    @Max(value = 65535, message = "port must be <= 65535")
    private Integer port;

    @NotBlank(message = "networkScope is required")
    private String networkScope;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPrivateIp() {
        return privateIp;
    }

    public void setPrivateIp(String privateIp) {
        this.privateIp = privateIp;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNetworkScope() {
        return networkScope;
    }

    public void setNetworkScope(String networkScope) {
        this.networkScope = networkScope;
    }

    @Override
    public String toString() {
        return "CreateRequest{" +
                "name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", privateIp='" + privateIp + '\'' +
                ", description='" + description + '\'' +
                ", port=" + port +
                ", networkScope='" + networkScope + '\'' +
                '}';
    }
}