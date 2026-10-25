package com.example.demo.integration.controller;

import com.example.demo.config.Constants;
import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegistryControllerIT {

    private static final String CREATE_BODY = """
        {
          "name": "web-server",
          "category": "application",
          "privateIp": "10.0.0.1",
          "description": "main web server",
          "port": 8080,
          "networkScope": "default"
        }
        """;

    private static final String IP_BODY = """
        {"ip": "10.0.0.1"}
        """;
    
    private static final String CREATE_TABLE = """
	    	CREATE TABLE IF NOT EXISTS %s (
	    	    id            UUID PRIMARY KEY,
	    	    name          VARCHAR(255)  NOT NULL,
	    	    category      VARCHAR(100),
	    	    private_ip    VARCHAR(45),
	    	    port          INT           NOT NULL,
	    	    description   VARCHAR(1000),
	    	    network_scope VARCHAR(150)  NOT NULL DEFAULT 'default',
	    	    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
	    	    updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
	    	)
	    	""".formatted(Constants.ENTITY);


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
	jdbcTemplate.execute(CREATE_TABLE);
	jdbcTemplate.execute("DELETE FROM " + Constants.ENTITY);
    }
    
    // ------------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------------

    @Test
    void createRegistry_shouldReturn200AndGeneratedId() throws Exception {
        mockMvc.perform(post("/ports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CREATE_BODY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isString());
    }

    @Test
    void createRegistry_whenMalformedJson_shouldReturn400() throws Exception {
        mockMvc.perform(post("/ports")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ not valid json"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createRegistry_whenUnsupportedMediaType_shouldReturn415() throws Exception {
        mockMvc.perform(post("/ports")
                .contentType(MediaType.TEXT_PLAIN)
                .content(CREATE_BODY))
            .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void createRegistry_whenAllRequiredFieldsMissing_shouldReturn400() throws Exception {
        mockMvc.perform(post("/ports")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        // blank name
        """
        {"name": " ", "category": "application", "privateIp": "10.0.0.1", "port": 8080, "networkScope": "default"}
        """,
        // invalid IPv4
        """
        {"name": "svc", "category": "application", "privateIp": "999.1.1.1", "port": 8080, "networkScope": "default"}
        """,
        // port too low
        """
        {"name": "svc", "category": "application", "privateIp": "10.0.0.1", "port": 0, "networkScope": "default"}
        """,
        // port too high
        """
        {"name": "svc", "category": "application", "privateIp": "10.0.0.1", "port": 65536, "networkScope": "default"}
        """,
        // missing port
        """
        {"name": "svc", "category": "application", "privateIp": "10.0.0.1", "networkScope": "default"}
        """,
        // blank networkScope
        """
        {"name": "svc", "category": "application", "privateIp": "10.0.0.1", "port": 8080, "networkScope": ""}
        """
    })
    void createRegistry_whenInvalidBody_shouldReturn400(String body) throws Exception {
        mockMvc.perform(post("/ports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------------
    // RETRIEVE
    // ------------------------------------------------------------------------

    @Test
    void findRegistry_afterCreate_shouldReturnPersistedRegistry() throws Exception {
        String id = createRegistryAndGetId();

        mockMvc.perform(get("/ports/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.name").value("web-server"))
            .andExpect(jsonPath("$.privateIp").value("10.0.0.1"))
            .andExpect(jsonPath("$.port").value(8080));
    }

    @Test
    void findRegistry_whenUnknownId_shouldReturn404() throws Exception {
        mockMvc.perform(get("/ports/{id}", UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }

    @Test
    void findRegistry_whenMalformedUuid_shouldReturn400() throws Exception {
        mockMvc.perform(get("/ports/{id}", "not-a-uuid"))
            .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------------
    // SEARCH
    // ------------------------------------------------------------------------

    @Test
    void search_withNoFilters_shouldReturnCreatedRegistries() throws Exception {
        createRegistryAndGetId();

        mockMvc.perform(post("/ports/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void search_withMatchingFilter_shouldReturnMatch() throws Exception {
        createRegistryAndGetId();

        mockMvc.perform(post("/ports/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"filters": {"name": ["web-server"]}, "page": 0, "size": 10}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void search_whenNothingMatches_shouldReturnEmptyList() throws Exception {
        createRegistryAndGetId();

        mockMvc.perform(post("/ports/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"filters": {"name": ["does-not-exist"]}}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void search_whenUnknownJsonProperty_shouldBeIgnored() throws Exception {
        mockMvc.perform(post("/ports/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"somethingElse": 1}
                    """))
            .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        """
        {"page": -1}
        """,
        """
        {"size": 0}
        """,
        """
        {"size": 101}
        """
    })
    void search_whenInvalidPaging_shouldReturn400(String body) throws Exception {
        mockMvc.perform(post("/ports/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------------
    // ASSIGN / AVAILABLE
    // ------------------------------------------------------------------------

    @Test
    void assign_afterCreate_shouldReturn200() throws Exception {
        createRegistryAndGetId();

        mockMvc.perform(post("/ports/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(IP_BODY))
            .andExpect(status().isOk());
    }

    @Test
    void fetchAvailablePorts_shouldReturn200() throws Exception {
        mockMvc.perform(post("/ports/available")
                .contentType(MediaType.APPLICATION_JSON)
                .content(IP_BODY))
            .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/ports/assign", "/ports/available"})
    void ipEndpoints_whenInvalidIp_shouldReturn400(String url) throws Exception {
        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"ip": "300.1.1.1"}
                    """))
            .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/ports/assign", "/ports/available"})
    void ipEndpoints_whenIpMissing_shouldReturn400(String url) throws Exception {
        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------------

    @Test
    void deleteRegistry_afterCreate_shouldReturn204AndRemoveRegistry() throws Exception {
        String id = createRegistryAndGetId();

        mockMvc.perform(delete("/ports/{id}", id))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/ports/{id}", id))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteRegistry_whenUnknownId_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/ports/{id}", UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------

    private String createRegistryAndGetId() throws Exception {
        String json = mockMvc.perform(post("/ports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CREATE_BODY))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        return JsonPath.read(json, "$.id");
    }
}