package com.example.demo.model.response;

import java.time.LocalDateTime;
import java.util.UUID;

public class RegistryResponse {
    private UUID id;
    private String name;
    private String category;
    private String privateIp;
    private Integer port;
    private String description;
    private String networkScope;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UUID getId() {
	return id;
    }

    public void setId(UUID id) {
	this.id = id;
    }

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

    public LocalDateTime getCreatedAt() {
	return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
	this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
	return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
	this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
	return "PostRegistryResponse={" + "id='" + id + '\'' + ", name='" + name + '\'' + ", category='" + category
		+ '\'' + ", privateIp='" + privateIp + '\'' + ", port=" + port + ", description='" + description + '\''
		+ ", networkScope='" + networkScope + '\'' + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt
		+ '}';
    }
}