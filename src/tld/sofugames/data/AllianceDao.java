package tld.sofugames.data;

import tld.sofugames.models.King;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class AllianceeDao extends PersistentData implements Dao<Object> {

	Connection connection;

	public AllianceDao() {
		connection = Data.getInstance().getConnection();
	}

	@Override
	public Optional<Object> get(String query) {
		return Optional.empty();
	}

	@Override
	public Map<String, Object> getAll() {
		PreparedStatement stmt;
		ResultSet results;
		stmt = (PreparedStatement) connection.prepareStatement("SELECT * FROM alliances");
		results = stmt.executeQuery();
		while(results.next()) {
			King king = kingData.get(UUID.fromString(results.getString("king1")).toString());
			king.allies.add(kingData.get(UUID.fromString(results.getString("king2")).toString()));
		}
	}

	@Override
	public void save(Object o) {

	}

	@Override
	public void update(Object o, Map<String, Object> params) {

	}

	@Override
	public void delete(Object o) {

	}
}
