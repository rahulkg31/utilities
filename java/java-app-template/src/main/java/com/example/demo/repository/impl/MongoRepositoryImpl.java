package com.example.demo.repository.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.mongodb.ReadPreference;
import com.mongodb.client.result.DeleteResult;

import com.example.demo.config.Constants;
import com.example.demo.entity.RegistryEntity;
import com.example.demo.enums.Field;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.RegistryRepository;

@Repository
@ConditionalOnProperty(name = "db.type", havingValue = "mongodb")
public class MongoRepositoryImpl implements RegistryRepository {
	private static final Logger LOGGER = LogManager.getLogger(MongoRepositoryImpl.class);

	private static final int MAX_PAGE_SIZE = 500;

	private final MongoTemplate mongoTemplate;

	public MongoRepositoryImpl(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public RegistryEntity create(RegistryEntity entity) {
		return mongoTemplate.insert(entity);
	}

	@Override
	public List<RegistryEntity> findByPrivateIp(String ip) {
		Query query = Query.query(Criteria.where(Constants.PRIVATE_IP_COL).is(ip));
		return mongoTemplate.find(query, RegistryEntity.class);
	}

	@Override
	public RegistryEntity find(UUID id) {
		return mongoTemplate.findById(id, RegistryEntity.class);
	}

	@Override
	public List<RegistryEntity> find(Map<String, List<Object>> filters, int page, int size) {
		Query query = new Query();

		addFilters(query, filters);
		addPagination(query, page, size);

		LOGGER.debug("Executing registry search. page={}, size={}, filterCount={}",
				page, size, filters == null ? 0 : filters.size());

		return mongoTemplate.find(query, RegistryEntity.class);
	}

	@Override
	public void delete(UUID id) {
		Query query = Query.query(Criteria.where("_id").is(id));
		DeleteResult result = mongoTemplate.remove(query, RegistryEntity.class);

		if (result.getDeletedCount() == 0) {
			LOGGER.warn("Registry deletion failed: no registry found for id={}", id);
			throw new NotFoundException("No RegistryEntity found to delete for id: " + id);
		}
	}

	@Override
	public int getMaxPortByPrivateIp(String ip) {
		Query query = Query.query(Criteria.where(Constants.PRIVATE_IP_COL).is(ip))
				.with(Sort.by(Sort.Direction.DESC, Constants.PORT_COL))
				.limit(1)
				// avoid stale reads from secondaries when allocating ports
				.withReadPreference(ReadPreference.primary());

		query.fields().include(Constants.PORT_COL);

		RegistryEntity top = mongoTemplate.findOne(query, RegistryEntity.class);
		return top != null ? top.getPort() : 0;   // adjust if getPort() returns a nullable type
	}

	// ------------------------------------------------------------------------
	// HELPER METHODS
	// ------------------------------------------------------------------------

	private void addFilters(Query query, Map<String, List<Object>> filters) {
		if (filters == null || filters.isEmpty()) {
			return;
		}

		List<Criteria> conditions = new ArrayList<>();

		for (Map.Entry<String, List<Object>> filter : filters.entrySet()) {
			String apiField = filter.getKey();
			validateFilterField(apiField);

			String dbColumn = Field.getColumnFromValue(apiField);
			if (dbColumn == null) {
				throw new IllegalArgumentException("Invalid filter field: '" + apiField + "'");
			}

			List<Object> values = filter.getValue();
			if (values == null || values.isEmpty()) {
				continue;
			}

			conditions.add(values.size() == 1
					? Criteria.where(dbColumn).is(values.get(0))
					: Criteria.where(dbColumn).in(values));
		}

		if (!conditions.isEmpty()) {
			query.addCriteria(new Criteria().andOperator(conditions));
		}
	}

	private void validateFilterField(String apiField) {
		if (Field.ID.getValue().equals(apiField)) {
			throw new IllegalArgumentException(
					"Filtering by 'id' is not allowed. Use find(id) instead.");
		}
	}

	private void addPagination(Query query, int page, int size) {
		if (page < 0 || size <= 0 || size > MAX_PAGE_SIZE) {
			throw new IllegalArgumentException(
					"Invalid pagination parameters: page=" + page + ", size=" + size
							+ " (size must be 1-" + MAX_PAGE_SIZE + ")");
		}

		query.with(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id")));
	}
}