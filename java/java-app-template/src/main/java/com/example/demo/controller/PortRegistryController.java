package com.example.demo.controller;

import com.example.demo.model.request.CreateRequest;
import com.example.demo.model.request.IpRequest;
import com.example.demo.model.request.SearchRequest;
import com.example.demo.model.response.IdResponse;
import com.example.demo.model.response.AssignmentResponse;
import com.example.demo.model.response.AvailabilityResponse;
import com.example.demo.model.response.RegistryResponse;
import com.example.demo.service.RegistryService;

import jakarta.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST endpoints for creating, querying, assigning and deleting port registries.
 */
@RestController
@RequestMapping("/ports")
public class PortRegistryController {

    private static final Logger LOGGER = LogManager.getLogger(PortRegistryController.class);

    private final RegistryService portRegistryService;

    public PortRegistryController(RegistryService portRegistryService) {
        this.portRegistryService = portRegistryService;
    }

    // ------------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------------

    @PostMapping
    public ResponseEntity<IdResponse> createRegistry(@Valid @RequestBody CreateRequest request) {
        LOGGER.debug("Create port registry request: {}", request);

        IdResponse response = portRegistryService.create(request);

        LOGGER.info("Port registry created: {}", response);
        return ResponseEntity.ok(response);
    }

    // ------------------------------------------------------------------------
    // RETRIEVE
    // ------------------------------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<RegistryResponse> findRegistry(@PathVariable UUID id) {
        LOGGER.debug("Find port registry request: id={}", id);

        return ResponseEntity.ok(portRegistryService.find(id));
    }

    @PostMapping("/search")
    public ResponseEntity<List<RegistryResponse>> search(@Valid @RequestBody SearchRequest request) {
        LOGGER.debug("Search port registries request: {}", request);

        List<RegistryResponse> response = portRegistryService.search(request);

        LOGGER.debug("Search port registries result: resultCount={}", response.size());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/available")
    public ResponseEntity<AvailabilityResponse> fetchAvailablePorts(@Valid @RequestBody IpRequest request) {
        LOGGER.debug("Fetch available ports request: {}", request);

        return ResponseEntity.ok(portRegistryService.fetchAvailablePorts(request));
    }

    // ------------------------------------------------------------------------
    // ASSIGN
    // ------------------------------------------------------------------------

    @PostMapping("/assign")
    public ResponseEntity<AssignmentResponse> assign(@Valid @RequestBody IpRequest request) {
        LOGGER.debug("Assign port request: {}", request);

        AssignmentResponse response = portRegistryService.assign(request);

        LOGGER.info("Port assigned: {}", response);
        return ResponseEntity.ok(response);
    }

    // ------------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------------

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRegistry(@PathVariable UUID id) {
        LOGGER.debug("Delete port registry request: id={}", id);

        portRegistryService.delete(id);

        LOGGER.info("Port registry deleted: id={}", id);
        return ResponseEntity.noContent().build();
    }
}