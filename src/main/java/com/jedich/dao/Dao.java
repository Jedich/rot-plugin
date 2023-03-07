package com.jedich.dao;

import java.util.Map;
import java.util.Optional;

public interface Dao<T> {
	default Optional<T> get(String query) {
		return Optional.ofNullable(getAll().get(query));
	}

	Map<String, T> getAll();

	void save(T t);

	void update(T t, Map<String, Object> params);

	void delete(T t);
}
