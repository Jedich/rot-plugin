package tld.sofugames.data;

import org.bukkit.Bukkit;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import tld.sofugames.models.Advisor;
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
				try {
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
					new DaoFactory().getClaims().getAll();
					return PersistentData.getInstance().kingData;
				} finally {
					PreparedStatement stmt;
					ResultSet results;
					stmt = (PreparedStatement) connection.prepareStatement("SELECT * FROM alliances");
					results = stmt.executeQuery();
					while(results.next()) {
						King king = kingData.get(UUID.fromString(results.getString("king1")).toString());
						king.allies.add(kingData.get(UUID.fromString(results.getString("king2")).toString()));
					}
					stmt = (PreparedStatement) connection.prepareStatement("SELECT * FROM kingdom_helpers");
					results = stmt.executeQuery();
					while(results.next()) {
						King parent = kingData.get(UUID.fromString(results.getString("of_king")).toString());
						new Advisor(
								Bukkit.getPlayer(UUID.fromString(results.getString("name"))),
								parent.homeChunk, parent
						);
						parent.advisors.add(UUID.fromString(results.getString("name")));
					}
					stmt = (PreparedStatement) connection.prepareStatement("SELECT * FROM war_claims");
					results = stmt.executeQuery();
					while(results.next()) {
						kingData.get(results.getString("by_king")).warClaims.add(
								claimData.get(results.getString("chunk_name")));
					}
					stmt = (PreparedStatement) connection.prepareStatement("SELECT * FROM relations");
					results = stmt.executeQuery();
					while(results.next()) {
						kingData.get(results.getString("name")).relations
								.put(kingData.get(results.getString("meaning_of")).getUuid(),
										results.getInt("value"));
					}
				}
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void save(King king) {
		try {
			PreparedStatement stmt = (PreparedStatement) Data.getInstance().getConnection().prepareStatement(
					"INSERT INTO kings VALUES(?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, king.assignedPlayer.getUniqueId().toString());
			stmt.setString(2, king.getTitle());
			stmt.setString(3, king.kingdomName);
			stmt.setString(4, king.homeChunk.chunkId);
			stmt.setInt(5, king.kingdomLevel);
			stmt.setInt(6, king.getCurrentGen());
			stmt.setFloat(7, king.getGoldBalance());
			stmt.executeUpdate();
			ResultSet retrievedId = stmt.getGeneratedKeys();
			if(retrievedId.next()) {
				king.setId(retrievedId.getInt(1));
			}
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
			sql.append("WHERE id = ").append(t.getId());
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
