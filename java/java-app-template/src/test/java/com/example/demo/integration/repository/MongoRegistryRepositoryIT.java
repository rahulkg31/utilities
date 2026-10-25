package com.example.demo.integration.repository;

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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test-mongodb")
@Testcontainers
class MongoRegistryRepositoryIT {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
	registry.add("db.mongodb.url", MONGO::getConnectionString);
    }

    @Autowired
    private RegistryRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
	mongoTemplate.remove(new Query(), RegistryEntity.class);
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
    void create_withExistingId_shouldFailInsteadOfOverwriting() {
	RegistryEntity first = save("a", "10.0.0.1", 8080);

	RegistryEntity duplicate = newEntity("b", "10.0.0.2", 8081);
	duplicate.setId(first.getId());

	assertThatThrownBy(() -> repository.create(duplicate)).isInstanceOf(DuplicateKeyException.class);

	assertThat(repository.find(first.getId()).getName()).isEqualTo("a");
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
		Arguments.of(Map.of("name", List.of()), 0, 10)); // filter with no values
    }

    @ParameterizedTest
    @MethodSource("inputsThatShouldReturnAll")
    void find_withNoEffectiveFilter_shouldReturnAll(Map<String, List<Object>> filters, int page, int size) {
	save("a", "10.0.0.1", 8080);
	save("b", "10.0.0.2", 8081);

	assertThat(repository.find(filters, page, size)).hasSize(2);
    }

    static Stream<Arguments> invalidPaging() {
	return Stream.of(Arguments.of(-1, 10), // negative page
		Arguments.of(0, 0), // zero size
		Arguments.of(0, -5), // negative size
		Arguments.of(0, 501)); // above MAX_PAGE_SIZE
    }

    @ParameterizedTest
    @MethodSource("invalidPaging")
    void find_withInvalidPagination_shouldBeRejected(int page, int size) {
	save("a", "10.0.0.1", 8080);

	assertThatThrownBy(() -> repository.find(null, page, size))
		.isInstanceOf(InvalidDataAccessApiUsageException.class).hasMessageContaining("Invalid pagination");
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
    void find_withMultiValueFilter_shouldMatchAnyValue() {
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

	assertThatThrownBy(() -> repository.find(filters, 0, 10)).isInstanceOf(InvalidDataAccessApiUsageException.class)
		.hasMessageContaining(field);
    }

    @Test
    void find_shouldPaginateWithoutOverlapOrGaps() {
	Set<UUID> expected = new HashSet<>();
	for (int i = 0; i < 5; i++) {
	    expected.add(save("svc-" + i, "10.0.0." + i, 8000 + i).getId());
	}

	List<RegistryEntity> page0 = repository.find(null, 0, 2);
	List<RegistryEntity> page1 = repository.find(null, 1, 2);
	List<RegistryEntity> page2 = repository.find(null, 2, 2);

	assertThat(page0).hasSize(2);
	assertThat(page1).hasSize(2);
	assertThat(page2).hasSize(1);
	assertThat(repository.find(null, 10, 2)).isEmpty();

	// Results are sorted, so pages must not overlap and together must cover
	// everything
	List<UUID> seen = new ArrayList<>();
	Stream.of(page0, page1, page2).flatMap(List::stream).forEach(e -> seen.add(e.getId()));

	assertThat(seen)
	    .doesNotHaveDuplicates()
	    .containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void find_shouldReturnSamePageOnRepeatedCalls() {
	for (int i = 0; i < 5; i++) {
	    save("svc-" + i, "10.0.0." + i, 8000 + i);
	}

	List<UUID> first = repository.find(null, 1, 2).stream().map(RegistryEntity::getId).toList();
	List<UUID> second = repository.find(null, 1, 2).stream().map(RegistryEntity::getId).toList();

	assertThat(second).containsExactlyElementsOf(first);
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

    @Test
    void getMaxPortByPrivateIp_shouldSeeJustInsertedRow() {
	// Guards against stale secondary reads: the query is forced to primary
	for (int port = 8000; port < 8020; port++) {
	    save("svc-" + port, "10.0.0.1", port);
	    assertThat(repository.getMaxPortByPrivateIp("10.0.0.1")).isEqualTo(port);
	}
    }

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------

    private RegistryEntity newEntity(String name, String privateIp, int port) {
	RegistryEntity entity = new RegistryEntity();
	entity.setName(name);
	entity.setCategory("application");
	entity.setPrivateIp(privateIp);
	entity.setDescription("test entry");
	entity.setPort(port);
	entity.setNetworkScope("default");
	return entity;
    }

    private RegistryEntity save(String name, String privateIp, int port) {
	RegistryEntity entity = newEntity(name, privateIp, port);
	if (entity.getId() == null) {
	    entity.setId(UUID.randomUUID());
	}
	return repository.create(entity);
    }
}