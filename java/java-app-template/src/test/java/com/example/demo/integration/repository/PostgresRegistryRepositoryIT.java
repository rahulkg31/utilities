package com.example.demo.integration.repository;

import com.example.demo.config.Constants;
import com.example.demo.entity.RegistryEntity;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.RegistryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test-postgres")
@Testcontainers
class PostgresRegistryRepositoryIT {

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

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
	    .withDatabaseName("registry_test").withUsername("test").withPassword("test");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
	registry.add("db.jdbc.host", POSTGRES::getHost);
	registry.add("db.jdbc.port", POSTGRES::getFirstMappedPort);
	registry.add("db.jdbc.database", POSTGRES::getDatabaseName);
	registry.add("db.jdbc.username", POSTGRES::getUsername);
	registry.add("db.jdbc.password", POSTGRES::getPassword);
    }

    @Autowired
    private RegistryRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
	jdbcTemplate.execute(CREATE_TABLE);
	jdbcTemplate.execute("DELETE FROM " + Constants.ENTITY);
    }

    // ------------------------------------------------------------------------
    // create / find(id)
    // ------------------------------------------------------------------------

    @Test
    void create_shouldPersistEntityAndAssignId() {
	RegistryEntity saved = save("web-server", "10.0.0.1", 8080);

	assertThat(saved.getId()).isNotNull();
    }

    @Test
    void find_byId_shouldReturnPersistedEntity() {
	RegistryEntity saved = save("web-server", "10.0.0.1", 8080);

	RegistryEntity found = repository.find(saved.getId());

	assertThat(found).isNotNull();
	assertThat(found.getName()).isEqualTo("web-server");
	assertThat(found.getPrivateIp()).isEqualTo("10.0.0.1");
	assertThat(found.getPort()).isEqualTo(8080);
    }

    @Test
    void find_byId_whenUnknown_shouldReturnNull() {
	assertThat(repository.find(UUID.randomUUID())).isNull();
    }

    // ------------------------------------------------------------------------
    // find(filters, page, size)
    // ------------------------------------------------------------------------

    static Stream<Arguments> inputsThatShouldReturnAll() {
	return Stream.of(Arguments.of(null, 0, 10), // null filters
		Arguments.of(Map.of(), 0, 10), // empty filters
		Arguments.of(Map.of("name", List.of()), 0, 10), // filter with no values
		Arguments.of(null, -1, 10), // invalid page
		Arguments.of(null, 0, 0)); // invalid size
    }

    @ParameterizedTest
    @MethodSource("inputsThatShouldReturnAll")
    void find_withNoEffectiveFilterOrPaging_shouldReturnAll(Map<String, List<Object>> filters, int page, int size) {
	save("a", "10.0.0.1", 8080);
	save("b", "10.0.0.2", 8081);

	assertThat(repository.find(filters, page, size)).hasSize(2);
    }

    @Test
    void find_withSingleValueFilter_shouldReturnMatchesOnly() {
	save("a", "10.0.0.1", 8080);
	save("b", "10.0.0.2", 8081);

	List<RegistryEntity> result = repository.find(Map.of("name", List.of((Object) "a")), 0, 10);

	assertThat(result).hasSize(1);
	assertThat(result.get(0).getName()).isEqualTo("a");
    }

    @Test
    void find_withMultiValueFilter_shouldUseInClause() {
	save("d", "10.0.0.1", 8080);
	save("e", "10.0.0.2", 8081);
	save("f", "10.0.0.3", 8082);

	List<RegistryEntity> result = repository.find(Map.of("name", List.of((Object) "d", "f")), 0, 10);

	assertThat(result).extracting(RegistryEntity::getName).containsExactlyInAnyOrder("d", "f");
    }

    @Test
    void find_withMultipleFilters_shouldCombineWithAnd() {
	save("a", "10.0.0.1", 8080);
	save("a", "10.0.0.2", 8081);

	List<RegistryEntity> result = repository
		.find(Map.of("name", List.of((Object) "a"), "privateIp", List.of((Object) "10.0.0.2")), 0, 10);

	assertThat(result).hasSize(1);
	assertThat(result.get(0).getPrivateIp()).isEqualTo("10.0.0.2");
    }

    @Test
    void find_withNoMatch_shouldReturnEmptyList() {
	save("a", "10.0.0.1", 8080);

	assertThat(repository.find(Map.of("name", List.of((Object) "nope")), 0, 10)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = { "id", "bogus" })
    void find_withDisallowedFilterField_shouldBeRejected(String field) {
	Map<String, List<Object>> filters = Map.of(field, List.of((Object) "x"));

	assertThatThrownBy(() -> repository.find(filters, 0, 10))
		.isInstanceOfAny(IllegalArgumentException.class, InvalidDataAccessApiUsageException.class)
		.hasMessageContaining(field);
    }

    @Test
    void find_shouldPaginate() {
	for (int i = 0; i < 5; i++) {
	    save("svc-" + i, "10.0.0." + i, 8000 + i);
	}

	// no ORDER BY in the query, so only sizes are asserted, not which rows
	assertThat(repository.find(null, 0, 2)).hasSize(2);
	assertThat(repository.find(null, 1, 2)).hasSize(2);
	assertThat(repository.find(null, 2, 2)).hasSize(1);
	assertThat(repository.find(null, 10, 2)).isEmpty();
    }

    // ------------------------------------------------------------------------
    // delete
    // ------------------------------------------------------------------------

    @Test
    void delete_shouldRemoveOnlyTargetEntity() {
	RegistryEntity a = save("a", "10.0.0.1", 8080);
	RegistryEntity b = save("b", "10.0.0.2", 8081);

	repository.delete(a.getId());

	assertThat(repository.find(a.getId())).isNull();
	assertThat(repository.find(b.getId())).isNotNull();
    }

    @Test
    void delete_whenUnknownId_shouldThrowNotFound() {
	UUID unknownId = UUID.randomUUID();

	assertThatThrownBy(() -> repository.delete(unknownId)).isInstanceOf(NotFoundException.class);
    }

    // ------------------------------------------------------------------------
    // findByPrivateIp
    // ------------------------------------------------------------------------

    @Test
    void findByPrivateIp_shouldReturnOnlyMatchingIp() {
	save("a", "10.1.0.1", 8080);
	save("b", "10.1.0.1", 8081);
	save("c", "10.0.0.2", 8082);

	assertThat(repository.findByPrivateIp("10.1.0.1")).extracting(RegistryEntity::getName)
		.containsExactlyInAnyOrder("a", "b");
    }

    @Test
    void findByPrivateIp_whenNoneMatch_shouldReturnEmptyList() {
	save("a", "10.0.0.1", 8080);

	assertThat(repository.findByPrivateIp("192.168.1.1")).isEmpty();
    }

    // ------------------------------------------------------------------------
    // getMaxPortByPrivateIp
    // ------------------------------------------------------------------------

    @Test
    void getMaxPortByPrivateIp_whenNoRows_shouldReturnZero() {
	assertThat(repository.getMaxPortByPrivateIp("10.1.1.1")).isZero();
    }

    @Test
    void getMaxPortByPrivateIp_shouldReturnHighestPortForThatIpOnly() {
	save("a", "10.0.0.1", 8080);
	save("b", "10.0.0.1", 9090);
	save("c", "10.0.0.2", 60000);

	assertThat(repository.getMaxPortByPrivateIp("10.0.0.1")).isEqualTo(9090);
    }

    // ------------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------------

    private RegistryEntity save(String name, String privateIp, int port) {
	RegistryEntity entity = new RegistryEntity();
	entity.setId(UUID.randomUUID());
	entity.setName(name);
	entity.setCategory("application");
	entity.setPrivateIp(privateIp);
	entity.setDescription("test entry");
	entity.setPort(port);
	entity.setNetworkScope("default");
	return repository.create(entity);
    }
}