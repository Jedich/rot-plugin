package tld.sofugames.data;

import tld.sofugames.models.King;
import tld.sofugames.models.Relation;

import java.sql.*;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class AllianceDao extends PersistentData implements Dao<Relation> {

	Connection connection;

	public AllianceDao() {
		connection = Data.getInstance().getConnection();
	}

	@Override
	public Optional<Relation> get(String query) {
		return Optional.empty();
	}

	@Override
	public Map<String, Relation> getAll() {
		try {
			PreparedStatement stmt;
			ResultSet results;
			stmt = (PreparedStatement) connection.prepareStatement("SELECT * FROM alliances");
			results = stmt.executeQuery();
			while(results.next()) {
				King king = kingData.get(UUID.fromString(results.getString("king1")).toString());
				king.allies.add(kingData.get(UUID.fromString(results.getString("king2")).toString()));
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void save(Relation o) {
		try {
			PreparedStatement pstmt = (PreparedStatement) Data.getInstance().getConnection().prepareStatement(
					"INSERT INTO alliances(king1, king2) VALUES(?, ?)", Statement.RETURN_GENERATED_KEYS);
			setStrings(o, pstmt);
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(Relation o, Map<String, Object> params) {

	}

	@Override
	public void delete(Relation o) {
		try {
			PreparedStatement pstmt = (PreparedStatement) Data.getInstance().getConnection().prepareStatement(
					"DELETE FROM alliances WHERE king1 = ?, king2 = ?");
			setStrings(o, pstmt);
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	private void setStrings(Relation o, PreparedStatement pstmt) throws SQLException {
		pstmt.setString(1, o.getKing1().getUuid().toString());
		pstmt.setString(2, o.getKing2().getUuid().toString());
		pstmt.executeUpdate();
		pstmt.setString(1, o.getKing2().getUuid().toString());
		pstmt.setString(2, o.getKing1().getUuid().toString());
		pstmt.executeUpdate();
	}
}
