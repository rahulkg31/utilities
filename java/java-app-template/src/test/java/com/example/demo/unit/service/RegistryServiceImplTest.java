package com.example.demo.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.example.demo.service.impl.RegistryServiceImpl;

@ExtendWith(MockitoExtension.class)
class RegistryServiceImplTest {

    @Mock
    private RegistryRepository portRegistryRepository;

    @InjectMocks
    private RegistryServiceImpl portRegistryService;

    // ------------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------------

    @Test
    void create_shouldCreateRegistrySuccessfully() {
        // Arrange
        CreateRequest request = createValidRequest();
        UUID id = UUID.randomUUID();

        when(portRegistryRepository.create(any(RegistryEntity.class)))
                .thenReturn(createEntity(id));

        // Act
        IdResponse response = portRegistryService.create(request);

        // Assert
        assertNotNull(response);
        assertEquals(id, response.getId());

        verify(portRegistryRepository).create(any(RegistryEntity.class));
    }

    @Test
    void create_shouldPassTrimmedValuesToRepository() {
        // Arrange
        CreateRequest request = createValidRequest();
        request.setName("  Test Registry  ");
        request.setPrivateIp("  192.168.1.10  ");

        when(portRegistryRepository.create(any(RegistryEntity.class)))
                .thenReturn(createEntity(UUID.randomUUID()));

        // Act
        portRegistryService.create(request);

        // Assert
        ArgumentCaptor<RegistryEntity> captor = ArgumentCaptor.forClass(RegistryEntity.class);
        verify(portRegistryRepository).create(captor.capture());

        RegistryEntity saved = captor.getValue();
        assertNotNull(saved.getId());
        assertEquals("Test Registry", saved.getName());
        assertEquals("192.168.1.10", saved.getPrivateIp());
        assertEquals(request.getCategory(), saved.getCategory());
        assertEquals(request.getPort(), saved.getPort());
    }

    @Test
    void create_shouldThrowNullPointerException_whenRequestIsNull() {
        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> portRegistryService.create(null));

        verifyNoInteractions(portRegistryRepository);
    }

    @Test
    void create_shouldThrowNullPointerException_whenNameIsNull() {
        // Arrange
        CreateRequest request = createValidRequest();
        request.setName(null);

        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> portRegistryService.create(request));

        verifyNoInteractions(portRegistryRepository);
    }

    @Test
    void create_shouldThrowNullPointerException_whenPrivateIpIsNull() {
        // Arrange
        CreateRequest request = createValidRequest();
        request.setPrivateIp(null);

        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> portRegistryService.create(request));

        verifyNoInteractions(portRegistryRepository);
    }

    @Test
    void create_shouldThrowException_whenCategoryIsNull() {
        // Arrange
        CreateRequest request = createValidRequest();
        request.setCategory(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> portRegistryService.create(request));

        assertEquals("Invalid category: null", exception.getMessage());

        verifyNoInteractions(portRegistryRepository);
    }

    @Test
    void create_shouldThrowException_whenCategoryIsInvalid() {
        // Arrange
        CreateRequest request = createValidRequest();
        request.setCategory("INVALID_CATEGORY");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> portRegistryService.create(request));

        assertEquals("Invalid category: INVALID_CATEGORY", exception.getMessage());

        verifyNoInteractions(portRegistryRepository);
    }

    @Test
    void create_shouldThrowException_whenRepositoryReturnsNull() {
        // Arrange
        CreateRequest request = createValidRequest();

        when(portRegistryRepository.create(any(RegistryEntity.class)))
                .thenReturn(null);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> portRegistryService.create(request));

        assertTrue(exception.getMessage()
                .startsWith("Failed to create port registry, repository returned no id"));
        assertTrue(exception.getMessage().contains("name='Test Registry'"));

        verify(portRegistryRepository).create(any(RegistryEntity.class));
    }

    @Test
    void create_shouldThrowException_whenCreatedEntityHasNullId() {
        // Arrange
        CreateRequest request = createValidRequest();

        when(portRegistryRepository.create(any(RegistryEntity.class)))
                .thenReturn(createEntity(null));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> portRegistryService.create(request));

        assertTrue(exception.getMessage()
                .startsWith("Failed to create port registry, repository returned no id"));

        verify(portRegistryRepository).create(any(RegistryEntity.class));
    }

    // ------------------------------------------------------------------------
    // FIND
    // ------------------------------------------------------------------------

    @Test
    void find_shouldReturnRegistrySuccessfully() {
        // Arrange
        UUID id = UUID.randomUUID();
        RegistryEntity entity = createEntity(id);

        when(portRegistryRepository.find(id)).thenReturn(entity);

        // Act
        RegistryResponse response = portRegistryService.find(id);

        // Assert
        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals(entity.getName(), response.getName());
        assertEquals(entity.getPrivateIp(), response.getPrivateIp());
        assertEquals(entity.getPort(), response.getPort());

        verify(portRegistryRepository).find(id);
    }

    @Test
    void find_shouldThrowNotFoundException_whenIdIsNull() {
        // The service does not validate the id; the repository finds nothing for null.

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> portRegistryService.find(null));

        assertEquals("No registry entry found for id: null", exception.getMessage());
    }

    @Test
    void find_shouldThrowNotFoundException_whenEntityDoesNotExist() {
        // Arrange
        UUID id = UUID.randomUUID();

        when(portRegistryRepository.find(id)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> portRegistryService.find(id));

        assertEquals("No registry entry found for id: " + id, exception.getMessage());

        verify(portRegistryRepository).find(id);
    }

    // ------------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------------

    @Test
    void delete_shouldDeleteRegistrySuccessfully() {
        // Arrange
        UUID id = UUID.randomUUID();

        when(portRegistryRepository.find(id)).thenReturn(createEntity(id));
        doNothing().when(portRegistryRepository).delete(id);

        // Act
        portRegistryService.delete(id);

        // Assert
        verify(portRegistryRepository).find(id);
        verify(portRegistryRepository).delete(id);
    }

    @Test
    void delete_shouldThrowNotFoundException_whenEntityDoesNotExist() {
        // Arrange
        UUID id = UUID.randomUUID();

        when(portRegistryRepository.find(id)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> portRegistryService.delete(id));

        assertEquals("No registry entry found for id: " + id, exception.getMessage());

        verify(portRegistryRepository, never()).delete(any(UUID.class));
    }

    @Test
    void delete_shouldThrowNotFoundException_whenIdIsNull() {
        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> portRegistryService.delete(null));

        assertEquals("No registry entry found for id: null", exception.getMessage());

        verify(portRegistryRepository, never()).delete(any(UUID.class));
    }

    // ------------------------------------------------------------------------
    // SEARCH
    // ------------------------------------------------------------------------

    @Test
    void search_shouldReturnRegistryList() {
        // Arrange
        SearchRequest request = mock(SearchRequest.class);

        RegistryEntity entity1 = createEntity(UUID.randomUUID());
        RegistryEntity entity2 = createEntity(UUID.randomUUID());

        when(portRegistryRepository.find(any(), anyInt(), anyInt()))
                .thenReturn(List.of(entity1, entity2));

        // Act
        List<RegistryResponse> response = portRegistryService.search(request);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(entity1.getId(), response.get(0).getId());
        assertEquals(entity2.getId(), response.get(1).getId());

        verify(portRegistryRepository).find(any(), anyInt(), anyInt());
    }

    @Test
    void search_shouldReturnEmptyList_whenRepositoryReturnsNull() {
        // Arrange
        SearchRequest request = mock(SearchRequest.class);

        when(portRegistryRepository.find(any(), anyInt(), anyInt()))
                .thenReturn(null);

        // Act
        List<RegistryResponse> response = portRegistryService.search(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(portRegistryRepository).find(any(), anyInt(), anyInt());
    }

    @Test
    void search_shouldReturnEmptyList_whenRepositoryReturnsEmptyList() {
        // Arrange
        SearchRequest request = mock(SearchRequest.class);

        when(portRegistryRepository.find(any(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        // Act
        List<RegistryResponse> response = portRegistryService.search(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(portRegistryRepository).find(any(), anyInt(), anyInt());
    }

    @Test
    void search_shouldThrowNullPointerException_whenRequestIsNull() {
        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> portRegistryService.search(null));

        verifyNoInteractions(portRegistryRepository);
    }

    // ------------------------------------------------------------------------
    // ASSIGN
    // ------------------------------------------------------------------------

    @Test
    void assign_shouldAssignDefaultPort_whenNoPortExists() {
        // Arrange
        IpRequest request = createIpRequest("192.168.1.10");

        when(portRegistryRepository.getMaxPortByPrivateIp("192.168.1.10"))
                .thenReturn(0);

        // Act
        AssignmentResponse response = portRegistryService.assign(request);

        // Assert
        assertNotNull(response);
        assertEquals("192.168.1.10", response.getIp());
        assertEquals(Constants.DEFAULT_PORT, response.getAssignedPort());

        verify(portRegistryRepository).getMaxPortByPrivateIp("192.168.1.10");
    }

    @Test
    void assign_shouldAssignNextPort_whenExistingPortIsHigherThanDefault() {
        // Arrange
        IpRequest request = createIpRequest("192.168.1.10");
        int maxPort = Constants.DEFAULT_PORT + 10;

        when(portRegistryRepository.getMaxPortByPrivateIp("192.168.1.10"))
                .thenReturn(maxPort);

        // Act
        AssignmentResponse response = portRegistryService.assign(request);

        // Assert
        assertEquals(maxPort + 1, response.getAssignedPort());
        assertEquals("192.168.1.10", response.getIp());

        verify(portRegistryRepository).getMaxPortByPrivateIp("192.168.1.10");
    }

    @Test
    void assign_shouldThrowNullPointerException_whenRequestIsNull() {
        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> portRegistryService.assign(null));

        verifyNoInteractions(portRegistryRepository);
    }

    // ------------------------------------------------------------------------
    // FETCH AVAILABLE PORTS
    // ------------------------------------------------------------------------

    @Test
    void fetchAvailablePorts_shouldReturnMissingPorts() {
        // Arrange
        String ip = "192.168.1.10";
        IpRequest request = createIpRequest(ip);
        int startPort = Constants.DEFAULT_PORT;

        when(portRegistryRepository.findByPrivateIp(ip))
                .thenReturn(List.of(
                        createEntityWithPort(startPort),
                        createEntityWithPort(startPort + 2)));

        // Act
        AvailabilityResponse response = portRegistryService.fetchAvailablePorts(request);

        // Assert
        assertNotNull(response);
        assertEquals(ip, response.getIp());

        // startPort used, startPort + 1 free, startPort + 2 used, maxPort + 1 always added
        assertEquals(List.of(startPort + 1, startPort + 3), response.getAvailablePorts());

        verify(portRegistryRepository).findByPrivateIp(ip);
    }

    @Test
    void fetchAvailablePorts_shouldReturnDefaultAndNextPort_whenNoExistingPorts() {
        // Arrange
        String ip = "192.168.1.10";
        IpRequest request = createIpRequest(ip);

        when(portRegistryRepository.findByPrivateIp(ip))
                .thenReturn(Collections.emptyList());

        // Act
        AvailabilityResponse response = portRegistryService.fetchAvailablePorts(request);

        // Assert
        assertNotNull(response);
        assertEquals(ip, response.getIp());

        // Nothing used: the default port itself is free, plus the next port after it
        assertEquals(
                List.of(Constants.DEFAULT_PORT, Constants.DEFAULT_PORT + 1),
                response.getAvailablePorts());

        verify(portRegistryRepository).findByPrivateIp(ip);
    }

    @Test
    void fetchAvailablePorts_shouldReturnOnlyNextPort_whenOnlyDefaultPortIsUsed() {
        // Arrange
        String ip = "192.168.1.10";
        IpRequest request = createIpRequest(ip);

        when(portRegistryRepository.findByPrivateIp(ip))
                .thenReturn(List.of(createEntityWithPort(Constants.DEFAULT_PORT)));

        // Act
        AvailabilityResponse response = portRegistryService.fetchAvailablePorts(request);

        // Assert
        assertEquals(List.of(Constants.DEFAULT_PORT + 1), response.getAvailablePorts());

        verify(portRegistryRepository).findByPrivateIp(ip);
    }

    @Test
    void fetchAvailablePorts_shouldIgnorePortsBelowDefaultPort() {
        // Arrange
        String ip = "192.168.1.10";
        IpRequest request = createIpRequest(ip);

        when(portRegistryRepository.findByPrivateIp(ip))
                .thenReturn(List.of(createEntityWithPort(Constants.DEFAULT_PORT - 1)));

        // Act
        AvailabilityResponse response = portRegistryService.fetchAvailablePorts(request);

        // Assert
        // The below-default port is ignored, so it behaves as if nothing is used
        assertEquals(
                List.of(Constants.DEFAULT_PORT, Constants.DEFAULT_PORT + 1),
                response.getAvailablePorts());

        verify(portRegistryRepository).findByPrivateIp(ip);
    }

    @Test
    void fetchAvailablePorts_shouldThrowNullPointerException_whenRequestIsNull() {
        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> portRegistryService.fetchAvailablePorts(null));

        verifyNoInteractions(portRegistryRepository);
    }

    // ------------------------------------------------------------------------
    // HELPER METHODS
    // ------------------------------------------------------------------------

    private CreateRequest createValidRequest() {
        CreateRequest request = new CreateRequest();

        request.setName("Test Registry");
        request.setCategory(validCategory());
        request.setPrivateIp("192.168.1.10");
        request.setPort(8080);
        request.setDescription("Test description");
        request.setNetworkScope("PRIVATE");

        return request;
    }

    private String validCategory() {
        return Category.values()[0].name();
    }

    private RegistryEntity createEntity(UUID id) {
        RegistryEntity entity = new RegistryEntity();

        entity.setId(id);
        entity.setName("Test Registry");
        entity.setCategory(validCategory());
        entity.setPrivateIp("192.168.1.10");
        entity.setPort(8080);
        entity.setDescription("Test description");
        entity.setNetworkScope("PRIVATE");

        return entity;
    }

    private RegistryEntity createEntityWithPort(int port) {
        RegistryEntity entity = createEntity(UUID.randomUUID());
        entity.setPort(port);
        return entity;
    }

    private IpRequest createIpRequest(String ip) {
        IpRequest request = new IpRequest();
        request.setIp(ip);
        return request;
    }
}