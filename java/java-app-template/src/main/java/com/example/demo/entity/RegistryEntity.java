package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import com.example.demo.config.Constants;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Entity(name = Constants.ENTITY)
@Table(name = Constants.ENTITY)
@Document(collection = Constants.ENTITY)
public class RegistryEntity {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = Constants.NAME_COL, nullable = false)
    @Field(Constants.NAME_COL)
    private String name;

    @Column(name = Constants.CATEGORY_COL)
    @Field(Constants.CATEGORY_COL)
    private String category;

    @Column(name = Constants.PRIVATE_IP_COL, nullable = false)
    @Field(Constants.PRIVATE_IP_COL)
    private String privateIp;

    @Column(name = Constants.PORT_COL, nullable = false)
    @Field(Constants.PORT_COL)
    private int port;

    @Column(name = Constants.DESCRIPTION_COL)
    @Field(Constants.DESCRIPTION_COL)
    private String description;

    @Column(name = Constants.NETWORK_SCOPE_COL, nullable = false)
    @Field(Constants.NETWORK_SCOPE_COL)
    private String networkScope;

    @Column(name = Constants.CREATED_AT_COL, nullable = false, updatable = false)
    @Field(Constants.CREATED_AT_COL)
    private LocalDateTime createdAt;

    @Column(name = Constants.UPDATED_AT_COL, nullable = false)
    @Field(Constants.UPDATED_AT_COL)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
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
        return "RegistryEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", privateIp='" + privateIp + '\'' +
                ", port=" + port +
                ", description='" + description + '\'' +
                ", networkScope='" + networkScope + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}