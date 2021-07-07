package tld.sofugames.data;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface Dao<T> {
	Optional<T> get(String query) throws SQLException;

	Map<String, T> getAll() throws SQLException;

	void save(T t) throws SQLException;

	void update(T t, Map<String, Object> params) throws SQLException;

	void delete(T t);
}
