package com.example.demo.util;

import java.util.UUID;

import com.example.demo.entity.RegistryEntity;
import com.example.demo.model.request.CreateRequest;
import com.example.demo.model.response.RegistryResponse;

/**
 * Utility class for mapping data between registry request DTOs, entity objects
 * and response DTOs.
 */
public class MapperUtils {

    private MapperUtils() {
    }

    public static RegistryEntity toEntity(CreateRequest request) {
	RegistryEntity entity = new RegistryEntity();
	entity.setId(UUID.randomUUID());
	entity.setName(request.getName().trim());
	entity.setCategory(request.getCategory());
	entity.setPrivateIp(request.getPrivateIp().trim());
	entity.setPort(request.getPort());
	entity.setDescription(request.getDescription());
	entity.setNetworkScope(request.getNetworkScope().trim());
	return entity;
    }

    public static RegistryResponse toRegistryResponse(RegistryEntity entity) {
	RegistryResponse response = new RegistryResponse();
	response.setId(entity.getId());
	response.setName(entity.getName());
	response.setCategory(entity.getCategory());
	response.setPrivateIp(entity.getPrivateIp());
	response.setPort(entity.getPort());
	response.setDescription(entity.getDescription());
	response.setNetworkScope(entity.getNetworkScope());
	response.setCreatedAt(entity.getCreatedAt());
	response.setUpdatedAt(entity.getUpdatedAt());
	return response;
    }
}