package com.example.demo.service.impl;

import com.example.demo.config.Constants;
import com.example.demo.entity.RegistryEntity;
import com.example.demo.enums.Category;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.request.CreateRequest;
import com.example.demo.model.request.IpRequest;
import com.example.demo.model.request.SearchRequest;
import com.example.demo.model.response.IdResponse;
import com.example.demo.model.response.AssignmentResponse;
import com.example.demo.model.response.AvailabilityResponse;
import com.example.demo.model.response.RegistryResponse;
import com.example.demo.repository.RegistryRepository;
import com.example.demo.service.RegistryService;
import com.example.demo.util.MapperUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Service implementation that manages port registry operations.
 */
@Service
public class RegistryServiceImpl implements RegistryService {

    private static final Logger LOGGER = LogManager.getLogger(RegistryServiceImpl.class);

    private final RegistryRepository portRegistryRepository;

    public RegistryServiceImpl(RegistryRepository portRegistryRepository) {
        this.portRegistryRepository = portRegistryRepository;
    }

    @Override
    public IdResponse create(CreateRequest request) {
        validateCategory(request.getCategory());

        RegistryEntity entity = portRegistryRepository.create(MapperUtils.toEntity(request));
        if (entity == null || entity.getId() == null) {
            throw new IllegalStateException(String.format(
                    "Failed to create port registry, repository returned no id: "
                            + "name='%s', category='%s', privateIp='%s', port=%s",
                    request.getName(), request.getCategory(), request.getPrivateIp(), request.getPort()));
        }

        LOGGER.debug("Port registry created: id={}", entity.getId());

        IdResponse response = new IdResponse();
        response.setId(entity.getId());
        return response;
    }
    
    @Override
    public RegistryResponse find(UUID id) {
        RegistryEntity entity = portRegistryRepository.find(id);
        if (entity == null) {
            throw new NotFoundException("No registry entry found for id: " + id);
        }

        LOGGER.debug("Port registry found: id={}", id);
        return MapperUtils.toRegistryResponse(entity);
    }

    @Override
    public List<RegistryResponse> search(SearchRequest request) {
        Map<String, List<Object>> filters = request.getFilters() != null
                ? request.getFilters()
                : Map.of();

        List<RegistryEntity> entities =
                portRegistryRepository.find(filters, request.getPage(), request.getSize());

        if (entities == null || entities.isEmpty()) {
            LOGGER.debug("No port registry entries matched search request");
            return List.of();
        }

        LOGGER.debug("Port registry search matched {} entries", entities.size());
        return entities.stream()
                .map(MapperUtils::toRegistryResponse)
                .toList();
    }

    @Override
    public AvailabilityResponse fetchAvailablePorts(IpRequest request) {
        String ip = request.getIp();
        List<Integer> availablePorts = findAvailablePorts(portRegistryRepository.findByPrivateIp(ip));

        LOGGER.debug("Found {} available ports for privateIp={}", availablePorts.size(), ip);

        AvailabilityResponse response = new AvailabilityResponse();
        response.setIp(ip);
        response.setAvailablePorts(availablePorts);
        return response;
    }

    @Override
    public AssignmentResponse assign(IpRequest request) {
        String ip = request.getIp();
        int maxPort = portRegistryRepository.getMaxPortByPrivateIp(ip);
        int assignedPort = Math.max(Constants.DEFAULT_PORT, maxPort + 1);

        LOGGER.debug("Assigned port={} for privateIp={}", assignedPort, ip);
        return new AssignmentResponse(ip, assignedPort);
    }

    @Override
    public void delete(UUID id) {
        // Same contract as find(): deleting an unknown id is a NotFoundException.
        if (portRegistryRepository.find(id) == null) {
            throw new NotFoundException("No registry entry found for id: " + id);
        }

        portRegistryRepository.delete(id);

        LOGGER.debug("Port registry deleted: id={}", id);
    }

    // ------------------------------------------------------------------------
    // HELPERS
    // ------------------------------------------------------------------------

    private void validateCategory(String category) {
        if (!Category.isValid(category)) {
            throw new IllegalArgumentException("Invalid category: " + category);
        }
    }

    /**
     * Returns every unused port between the default port and the highest used port,
     * followed by the next port after the highest one.
     */
    private List<Integer> findAvailablePorts(List<RegistryEntity> entities) {
        int startPort = Constants.DEFAULT_PORT;
        int maxPort = startPort;
        Set<Integer> usedPorts = new HashSet<>();

        for (RegistryEntity entity : entities) {
            int port = entity.getPort();
            if (port >= startPort) {
                usedPorts.add(port);
                maxPort = Math.max(maxPort, port);
            }
        }

        List<Integer> availablePorts = new ArrayList<>();
        for (int port = startPort; port <= maxPort; port++) {
            if (!usedPorts.contains(port)) {
                availablePorts.add(port);
            }
        }
        availablePorts.add(maxPort + 1);

        return availablePorts;
    }
}