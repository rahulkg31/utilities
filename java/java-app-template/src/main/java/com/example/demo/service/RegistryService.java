package com.example.demo.service;

import com.example.demo.model.request.IpRequest;
import com.example.demo.model.request.CreateRequest;
import com.example.demo.model.request.SearchRequest;
import com.example.demo.model.response.IdResponse;
import com.example.demo.model.response.AssignmentResponse;
import com.example.demo.model.response.AvailabilityResponse;
import com.example.demo.model.response.RegistryResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing port registry operations.
 *
 * <p>Provides operations to create, retrieve, search, assign, and delete
 * port registry entries.</p>
 */
public interface RegistryService {

    /**
     * Creates a new port registry entry.
     *
     * @param request request containing the registry details
     * @return response containing the ID of the newly created registry entry
     */
    IdResponse create(CreateRequest request);

    /**
     * Retrieves a port registry entry by its unique identifier.
     *
     * @param id unique identifier of the registry entry
     * @return the registry entry details
     */
    RegistryResponse find(UUID id);

    /**
     * Searches for port registry entries based on the specified search criteria.
     *
     * @param request search criteria including filters and pagination details
     * @return list of matching registry entries; an empty list if no entries are found
     */
    List<RegistryResponse> search(SearchRequest request);

    /**
     * Assigns the next available port for the specified private IP address.
     *
     * @param request request containing the private IP address
     * @return response containing the private IP address and the assigned port
     */
    AssignmentResponse assign(IpRequest request);

    /**
     * Retrieves the available ports for the specified private IP address.
     *
     * @param request request containing the private IP address
     * @return response containing the private IP address and a list of available ports
     */
    AvailabilityResponse fetchAvailablePorts(IpRequest request);

    /**
     * Deletes a port registry entry by its unique identifier.
     *
     * @param id unique identifier of the registry entry to delete
     */
    void delete(UUID id);
}

