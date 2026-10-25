package com.example.demo.repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface BaseRepository<T> {

	// ----------------------------
	// CREATE
	// ----------------------------

	/**
	 * Inserts a new entity.
	 */
	T create(T entity);

	// ----------------------------
	// READ
	// ----------------------------

	/**
	 * Finds an entity by its ID.
	 */
	T find(UUID id);

	/**
	 * Finds entities matching the search request criteria.
	 */
	List<T> find(Map<String, List<Object>> filters, int page, int size);

	// ----------------------------
	// DELETE
	// ----------------------------

	/**
	 * Deletes an entity by ID.
	 */
	void delete(UUID id);

}