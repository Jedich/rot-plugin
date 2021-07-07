package tld.sofugames.data;

import org.bukkit.Bukkit;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import tld.sofugames.models.King;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class KingDao extends PersistentData implements Dao<King> {
	private HashMap<UUID, King> kingdata = new HashMap<>();
	Connection connection;

	public KingDao() {
		connection = Data.getInstance().getConnection();
	}

	@Override
	public Optional<King> get(String query) throws SQLException {
		return Optional.of(getAll().get(query));
	}

	@Override
	public Map<String, King> getAll() throws SQLException {
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
		return PersistentData.getInstance().kingData;
	}

	@Override
	public void save(King king) throws SQLException {
		PreparedStatement pstmt = (PreparedStatement) Data.getInstance().getConnection().prepareStatement(
				"INSERT INTO kings VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
		pstmt.setString(1, king.assignedPlayer.getUniqueId().toString());
		pstmt.setString(2, king.getTitle());
		pstmt.setString(3, king.kingdomName);
		pstmt.setString(4, king.homeChunk.chunkId);
		pstmt.setInt(5, king.kingdomLevel);
		pstmt.setInt(6, king.getCurrentGen());
		pstmt.setFloat(7, king.getGoldBalance());
		pstmt.executeUpdate();
	}

	@Override
	public void update(King t, Map<String, Object> params) throws SQLException {
		StringBuilder sql = new StringBuilder("UPDATE kings SET ");
		for(Map.Entry<String, Object> entry : params.entrySet()) {
			sql.append(entry.getKey()).append(" = ");
			if(entry.getValue() instanceof String) {
				sql.append("'").append(entry.getValue()).append("', ");
			} else if(entry.getValue() instanceof Integer || entry.getValue() instanceof Float) {
				sql.append(entry.getValue()).append(", ");
			} else {
				throw new SQLException("Uncaught type.");
			}
		}
		sql.deleteCharAt(sql.length() - 2);
		sql.append("WHERE id = ").append(t.id);
		System.out.println(sql.toString());
		PreparedStatement pstmt = (PreparedStatement) Data.getInstance().getConnection().prepareStatement(sql.toString());
		//pstmt.setString(1, kingdomName);
		pstmt.executeUpdate();
	}

	@Override
	public void delete(King king) {
		throw new NotImplementedException();
	}
}
