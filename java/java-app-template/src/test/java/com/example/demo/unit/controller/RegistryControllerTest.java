package com.example.demo.unit.controller;

import com.example.demo.controller.PortRegistryController;
import com.example.demo.model.request.IpRequest;
import com.example.demo.model.request.CreateRequest;
import com.example.demo.model.request.SearchRequest;
import com.example.demo.model.response.IdResponse;
import com.example.demo.model.response.AssignmentResponse;
import com.example.demo.model.response.AvailabilityResponse;
import com.example.demo.model.response.RegistryResponse;
import com.example.demo.service.RegistryService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistryControllerTest {

    @Mock
    private RegistryService portRegistryService;

    @InjectMocks
    private PortRegistryController portRegistryController;


    // ------------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------------

    @Test
    void createRegistry_shouldReturnCreatedResponse() {

        // Arrange
        CreateRequest request = new CreateRequest();

        request.setName("Test Registry");
        request.setCategory("DATABASE");
        request.setPrivateIp("192.168.1.10");
        request.setPort(8080);
        request.setDescription("Test description");
        request.setNetworkScope("PRIVATE");

        IdResponse expectedResponse = new IdResponse();

        when(portRegistryService.create(request))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<IdResponse> response =
                portRegistryController.createRegistry(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());

        verify(portRegistryService, times(1))
                .create(request);
    }


    // ------------------------------------------------------------------------
    // RETRIEVE
    // ------------------------------------------------------------------------

    @Test
    void findRegistry_shouldReturnRegistryResponse() {

        // Arrange
        UUID id = UUID.randomUUID();

        RegistryResponse expectedResponse =
                new RegistryResponse();

        when(portRegistryService.find(id))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<RegistryResponse> response =
                portRegistryController.findRegistry(id);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());

        verify(portRegistryService, times(1))
                .find(id);
    }


    // ------------------------------------------------------------------------
    // SEARCH
    // ------------------------------------------------------------------------

    @Test
    void search_shouldReturnRegistryList() {

        // Arrange
        SearchRequest request = new SearchRequest();

        RegistryResponse registry1 =
                new RegistryResponse();

        RegistryResponse registry2 =
                new RegistryResponse();

        List<RegistryResponse> expectedResponse =
                List.of(registry1, registry2);

        when(portRegistryService.search(request))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<List<RegistryResponse>> response =
                portRegistryController.search(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertSame(
                expectedResponse,
                response.getBody()
        );

        assertEquals(
                2,
                response.getBody().size()
        );

        verify(portRegistryService, times(1))
                .search(request);
    }


    // ------------------------------------------------------------------------
    // ASSIGN
    // ------------------------------------------------------------------------

    @Test
    void assign_shouldReturnPortAssignmentResponse() {

        // Arrange
        IpRequest request = new IpRequest();

        AssignmentResponse expectedResponse =
                new AssignmentResponse();

        when(portRegistryService.assign(request))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<AssignmentResponse> response =
                portRegistryController.assign(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());

        verify(portRegistryService, times(1))
                .assign(request);
    }


    // ------------------------------------------------------------------------
    // AVAILABLE PORTS
    // ------------------------------------------------------------------------

    @Test
    void fetchAvailablePorts_shouldReturnAvailablePortsResponse() {

        // Arrange
        IpRequest request = new IpRequest();

        AvailabilityResponse expectedResponse =
                new AvailabilityResponse();

        when(portRegistryService.fetchAvailablePorts(request))
                .thenReturn(expectedResponse);

        // Act
        ResponseEntity<AvailabilityResponse> response =
                portRegistryController.fetchAvailablePorts(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());

        verify(portRegistryService, times(1))
                .fetchAvailablePorts(request);
    }


    // ------------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------------

    @Test
    void deleteRegistry_shouldReturnNoContent() {

        // Arrange
        UUID id = UUID.randomUUID();

        doNothing()
                .when(portRegistryService)
                .delete(id);

        // Act
        ResponseEntity<Void> response =
                portRegistryController.deleteRegistry(id);

        // Assert
        assertNotNull(response);
        assertEquals(
                HttpStatus.NO_CONTENT,
                response.getStatusCode()
        );

        assertNull(response.getBody());

        verify(portRegistryService, times(1))
                .delete(id);
    }
}
