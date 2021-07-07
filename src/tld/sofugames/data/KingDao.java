package tld.sofugames.data;

import org.bukkit.Bukkit;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import tld.sofugames.models.King;

import java.sql.*;
import java.util.*;

public class KingDao extends PersistentData implements Dao<King> {
	Connection connection;

	public KingDao() {
		connection = Data.getInstance().getConnection();
	}

	@Override
	public Optional<King> get(String query) {
		return Optional.of(getAll().get(query));
	}

	@Override
	public Map<String, King> getAll() {
		try {
			if(PersistentData.getInstance().kingData.size() == 0) {
				PreparedStatement stmt = (PreparedStatement) connection.prepareStatement("SELECT * FROM kings");
				ResultSet results = stmt.executeQuery();
				while(results.next()) {
					PersistentData.getInstance().kingData.put(results.getString("name"), new King(results.getInt("id"),
							Bukkit.getPlayer(UUID.fromString(results.getString("name"))),
							results.getString("title"),
							results.getString("kingdom_name"),
							results.getInt("kingdom_level"),
							results.getInt("current_gen"),
							results.getFloat("balance")
					));
				}
			}
			new DaoFactory().getClaims().getAll();
			return PersistentData.getInstance().kingData;
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void save(King king) {
		try {
			PreparedStatement pstmt = (PreparedStatement) Data.getInstance().getConnection().prepareStatement(
					"INSERT INTO kings VALUES(?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, king.assignedPlayer.getUniqueId().toString());
			pstmt.setString(2, king.getTitle());
			pstmt.setString(3, king.kingdomName);
			pstmt.setString(4, king.homeChunk.chunkId);
			pstmt.setInt(5, king.kingdomLevel);
			pstmt.setInt(6, king.getCurrentGen());
			pstmt.setFloat(7, king.getGoldBalance());
			pstmt.executeUpdate();
			getAll().put(king.getUuid().toString(), king);
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(King t, Map<String, Object> params) {
		try {
			StringBuilder sql = new StringBuilder("UPDATE kings SET ");
			for(Map.Entry<String, Object> entry : params.entrySet()) {
				sql.append(entry.getKey()).append(" = ");
				if(entry.getValue() instanceof String) {
					sql.append("'").append(entry.getValue()).append("', ");
				} else if(entry.getValue() instanceof Integer || entry.getValue() instanceof Float) {
					sql.append(entry.getValue()).append(", ");
				} else {
					throw new IllegalArgumentException("Uncaught type.");
				}
			}
			sql.deleteCharAt(sql.length() - 2);
			sql.append("WHERE id = ").append(t.id);
			System.out.println(sql.toString());
			PreparedStatement pstmt = (PreparedStatement) Data.getInstance().getConnection().prepareStatement(sql.toString());
			//pstmt.setString(1, kingdomName);
			pstmt.executeUpdate();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void delete(King king) {
		throw new NotImplementedException();
	}
}
