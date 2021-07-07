package tld.sofugames.data;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface Dao<T> {
	Optional<T> get(String query);

	Map<String, T> getAll();

	void save(T t);

	void update(T t, Map<String, Object> params);

	void delete(T t);
}
