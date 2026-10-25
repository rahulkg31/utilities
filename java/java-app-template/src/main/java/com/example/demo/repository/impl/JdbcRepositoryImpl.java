package com.example.demo.repository.impl;

import com.example.demo.config.Constants;
import com.example.demo.entity.RegistryEntity;
import com.example.demo.enums.Field;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.RegistryRepository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
@ConditionalOnProperty(name = "db.type", havingValue = "jdbc")
public class JdbcRepositoryImpl implements RegistryRepository {
	private static final Logger LOGGER = LogManager.getLogger(JdbcRepositoryImpl.class);

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	@Transactional // read-write
	public RegistryEntity create(RegistryEntity entity) {
		try {
			entityManager.persist(entity);
			entityManager.flush();
			return entity;
		} catch (Exception ex) {
			LOGGER.error(
					"Failed to create port registry entity. name={}, category={}",
					entity.getName(),
					entity.getCategory(),
					ex
			);
			throw ex;
		}
	}

	@Override
	public RegistryEntity find(UUID id) {
		try {
			return entityManager.find(RegistryEntity.class, id);
		} catch (Exception ex) {
			LOGGER.error("Failed to find port registry entity. id={}", id, ex);
			throw ex;
		}
	}

	@Override
	public List<RegistryEntity> find(
			Map<String, List<Object>> filters,
			int page,
			int size) {

		try {
			StringBuilder sql = new StringBuilder(
					"SELECT * FROM " + Constants.ENTITY + " WHERE 1=1"
			);

			Map<String, Object> parameters = new HashMap<>();

			addFilters(sql, parameters, filters);

			Query query = entityManager.createNativeQuery(
					sql.toString(),
					RegistryEntity.class
			);

			parameters.forEach(query::setParameter);

			addPagination(query, page, size);

			LOGGER.debug(
					"Executing port registry search. page={}, size={}, filterCount={}",
					page,
					size,
					parameters.size()
			);

			return query.getResultList();

		} catch (Exception ex) {
			LOGGER.error(
					"Failed to search port registry entities. page={}, size={}",
					page,
					size,
					ex
			);
			throw ex;
		}
	}

	@Override
	@Transactional // read-write
	public void delete(UUID id) {
		try {
			String sql = "DELETE FROM " + Constants.ENTITY + " WHERE id = :id";

			int rowsDeleted = entityManager
					.createNativeQuery(sql)
					.setParameter("id", id)
					.executeUpdate();

			if (rowsDeleted == 0) {
				// RuntimeException -> Spring rolls back automatically.
				// If NotFoundException is a CHECKED exception, use
				// @Transactional(rollbackFor = NotFoundException.class)
				throw new NotFoundException("Entity not found for ID: " + id);
			}

		} catch (Exception ex) {
			LOGGER.error(
					"Failed to delete port registry entity. id={}",
					id,
					ex
			);
			throw ex;
		}
	}

	@Override
	public List<RegistryEntity> findByPrivateIp(String ip) {
		try {
			String sql =
					"SELECT * FROM " + Constants.ENTITY
							+ " WHERE private_ip = :ip";

			return entityManager
					.createNativeQuery(sql, RegistryEntity.class)
					.setParameter("ip", ip)
					.getResultList();

		} catch (Exception ex) {
			LOGGER.error(
					"Failed to find port registry entities by private IP. ip={}",
					ip,
					ex
			);
			throw ex;
		}
	}

	@Override
	public int getMaxPortByPrivateIp(String ip) {
		try {
			String sql =
					"SELECT COALESCE(MAX(port), 0)"
							+ " FROM " + Constants.ENTITY
							+ " WHERE private_ip = :ip";

			Number result = (Number) entityManager
					.createNativeQuery(sql)
					.setParameter("ip", ip)
					.getSingleResult();

			return result.intValue();

		} catch (Exception ex) {
			LOGGER.error(
					"Failed to retrieve maximum port for private IP. ip={}",
					ip,
					ex
			);
			throw ex;
		}
	}

	// ------------------------------------------------------------------------
	// HELPER METHODS
	// ------------------------------------------------------------------------

	private void addFilters(
			StringBuilder sql,
			Map<String, Object> parameters,
			Map<String, List<Object>> filters) {

		if (filters == null || filters.isEmpty()) {
			return;
		}

		int parameterIndex = 0;

		for (Map.Entry<String, List<Object>> filter : filters.entrySet()) {
			String apiField = filter.getKey();

			validateFilterField(apiField);

			List<Object> values = filter.getValue();

			if (values == null || values.isEmpty()) {
				continue;
			}

			String column = Field.getColumnFromValue(apiField);
			String parameterName = "param" + parameterIndex++;

			appendFilter(sql, parameters, column, parameterName, values);
		}
	}

	private void validateFilterField(String apiField) {
		if ("id".equals(apiField)) {
			throw new IllegalArgumentException("Filtering by 'id' is not allowed.");
		}

		if (Field.getColumnFromValue(apiField) == null) {
			throw new IllegalArgumentException("Invalid filter: " + apiField);
		}
	}

	private void appendFilter(
			StringBuilder sql,
			Map<String, Object> parameters,
			String column,
			String parameterName,
			List<Object> values) {

		if (values.size() == 1) {
			sql.append(" AND ")
					.append(column)
					.append(" = :")
					.append(parameterName);

			parameters.put(parameterName, values.get(0));
			return;
		}

		sql.append(" AND ")
				.append(column)
				.append(" IN (:")
				.append(parameterName)
				.append(")");

		parameters.put(parameterName, values);
	}

	private void addPagination(Query query, int page, int size) {
		if (page >= 0 && size > 0) {
			query.setFirstResult(page * size);
			query.setMaxResults(size);
		}
	}
}