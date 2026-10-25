package com.example.demo.unit.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.example.demo.entity.RegistryEntity;
import com.example.demo.model.request.CreateRequest;
import com.example.demo.model.response.RegistryResponse;
import com.example.demo.util.MapperUtils;

class MapperUtilsTest {

    @Test
    void toEntity_shouldMapAllFields() {
        // Arrange
        CreateRequest request = new CreateRequest();
        request.setName("Test Registry");
        request.setCategory("DATABASE");
        request.setPrivateIp("192.168.1.10");
        request.setPort(8080);
        request.setDescription("Test description");
        request.setNetworkScope("PRIVATE");

        // Act
        RegistryEntity entity = MapperUtils.toEntity(request);

        // Assert
        assertNotNull(entity);
        assertNotNull(entity.getId());
        assertEquals("Test Registry", entity.getName());
        assertEquals("DATABASE", entity.getCategory());
        assertEquals("192.168.1.10", entity.getPrivateIp());
        assertEquals(8080, entity.getPort());
        assertEquals("Test description", entity.getDescription());
        assertEquals("PRIVATE", entity.getNetworkScope());

        // Timestamps are populated by @PrePersist, not by MapperUtils
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }

    @Test
    void toEntity_shouldTrimNameIpAndNetworkScope() {
        // Arrange
        CreateRequest request = new CreateRequest();
        request.setName("  Test Registry  ");
        request.setCategory("DATABASE");
        request.setPrivateIp("  192.168.1.10  ");
        request.setPort(8080);
        request.setDescription("Test description");
        request.setNetworkScope("  PRIVATE  ");

        // Act
        RegistryEntity entity = MapperUtils.toEntity(request);

        // Assert
        assertEquals("Test Registry", entity.getName());
        assertEquals("192.168.1.10", entity.getPrivateIp());
        assertEquals("PRIVATE", entity.getNetworkScope());
    }

    @Test
    void toEntity_shouldGenerateUniqueIdPerCall() {
        // Arrange
        CreateRequest request = new CreateRequest();
        request.setName("Test Registry");
        request.setCategory("DATABASE");
        request.setPrivateIp("192.168.1.10");
        request.setPort(8080);
        request.setDescription("Test description");
        request.setNetworkScope("PRIVATE");

        // Act
        RegistryEntity first = MapperUtils.toEntity(request);
        RegistryEntity second = MapperUtils.toEntity(request);

        // Assert
        assertNotNull(first.getId());
        assertNotNull(second.getId());
        assertNotEquals(first.getId(), second.getId());
    }

    @Test
    void toRegistryResponse_shouldMapAllFields() {
        // Arrange
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 9, 25, 10, 30, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 9, 25, 11, 30, 0);

        RegistryEntity entity = new RegistryEntity();
        entity.setId(id);
        entity.setName("Test Registry");
        entity.setCategory("DATABASE");
        entity.setPrivateIp("192.168.1.10");
        entity.setPort(8080);
        entity.setDescription("Test description");
        entity.setNetworkScope("PRIVATE");
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);

        // Act
        RegistryResponse response = MapperUtils.toRegistryResponse(entity);

        // Assert
        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals("Test Registry", response.getName());
        assertEquals("DATABASE", response.getCategory());
        assertEquals("192.168.1.10", response.getPrivateIp());
        assertEquals(8080, response.getPort());
        assertEquals("Test description", response.getDescription());
        assertEquals("PRIVATE", response.getNetworkScope());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
    }
}