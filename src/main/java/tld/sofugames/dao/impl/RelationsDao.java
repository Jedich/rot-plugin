package tld.sofugames.dao.impl;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import tld.sofugames.dao.Dao;
import tld.sofugames.dao.impl.PersistentData;
import tld.sofugames.data.Data;
import tld.sofugames.models.Relation;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class RelationsDao extends PersistentData implements Dao<Relation> {

	Connection connection;

	public RelationsDao() {
		connection = Data.getInstance().getConnection();
	}

	@Override
	public Map<String, Relation> getAll() {
		throw new NotImplementedException();
	}

	public HashMap<UUID, Integer> getByKingId(int kingId) {
		HashMap<UUID, Integer> relations = new HashMap<>();
		try {
			PreparedStatement stmt;
			ResultSet results;
			stmt = (PreparedStatement) connection.prepareStatement("SELECT * FROM relations WHERE king = ?");
			stmt.setInt(1, kingId);
			results = stmt.executeQuery();
			while(results.next()) {
				relations.put(UUID.fromString(results.getString("meaning_of")), results.getInt("value"));
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return relations;
	}

	@Override
	public void save(Relation o) {
		try {
			PreparedStatement pstmt = (PreparedStatement) Data.getInstance().getConnection().prepareStatement(
					"INSERT INTO relations(king, meaning_of, value) VALUES(?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			pstmt.setInt(1, o.getKing1().getId());
			pstmt.setString(2, o.getKing2().getUuid().toString());
			pstmt.setInt(3, o.getKing1().relations.get(o.getKing2().getUuid()));
			pstmt.executeUpdate();

			pstmt.setInt(1, o.getKing2().getId());
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
					"UPDATE relations SET value = ? WHERE king = ? AND meaning_of = ?");
			pstmt.setInt(1, o.getKing1().relations.get(o.getKing2().getUuid()));
			pstmt.setInt(2, o.getKing1().getId());
			pstmt.setString(3, o.getKing2().getUuid().toString());
			pstmt.executeUpdate();

			pstmt.setInt(1, o.getKing2().relations.get(o.getKing1().getUuid()));
			pstmt.setInt(2, o.getKing2().getId());
			pstmt.setString(3, o.getKing1().getUuid().toString());
			pstmt.executeUpdate();
			System.out.println("Relations between " +  o.getKing1().assignedPlayer.getName() + " and "
					+  o.getKing2().assignedPlayer.getName() + " have been updated.");
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void delete(Relation o) {
		throw new NotImplementedException();
	}
}
