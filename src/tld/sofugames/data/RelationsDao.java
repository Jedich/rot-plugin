package tld.sofugames.data;

import tld.sofugames.models.Relation;

import java.sql.*;
import java.util.Map;
import java.util.Optional;

public class RelationsDao extends PersistentData implements Dao<Relation> {

	Connection connection;

	public RelationsDao() {
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
			stmt = (PreparedStatement) connection.prepareStatement("SELECT * FROM relations");
			results = stmt.executeQuery();
			while(results.next()) {
				kingData.get(results.getString("name")).relations
						.put(kingData.get(results.getString("meaning_of")).getUuid(),
								results.getInt("value"));
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
					"INSERT INTO relations(name, meaning_of, value) VALUES(?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, o.getKing1().getUuid().toString());
			pstmt.setString(2, o.getKing2().getUuid().toString());
			pstmt.setInt(3, o.getKing1().relations.get(o.getKing2().getUuid()));
			pstmt.executeUpdate();

			pstmt.setString(1, o.getKing2().getUuid().toString());
			pstmt.setString(2, o.getKing1().getUuid().toString());
			pstmt.setInt(3, o.getKing2().relations.get(o.getKing1().getUuid()));
			pstmt.executeUpdate();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(Relation o, Map<String, Object> params) {
		try {
			PreparedStatement pstmt = (PreparedStatement) Data.getInstance().getConnection().prepareStatement(
					"UPDATE relations SET value=? WHERE name=? AND meaning_of=?");
			pstmt.setInt(1, o.getKing1().relations.get(o.getKing2().getUuid()));
			pstmt.setString(2, o.getKing1().getUuid().toString());
			pstmt.setString(3, o.getKing2().getUuid().toString());
			pstmt.executeUpdate();

			pstmt.setInt(1, o.getKing2().relations.get(o.getKing1().getUuid()));
			pstmt.setString(2, o.getKing2().getUuid().toString());
			pstmt.setString(3, o.getKing1().getUuid().toString());
			pstmt.executeUpdate();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void delete(Relation o) {

	}
}
