package tld.sofugames.dao.impl;

import org.bukkit.Bukkit;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import tld.sofugames.dao.Dao;
import tld.sofugames.data.Data;
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
	public Map<String, King> getAll() {
		try {
			if(PersistentData.getInstance().kingData.size() == 0) {
				PreparedStatement stmt = (PreparedStatement) connection.prepareStatement("SELECT * FROM kings");
				ResultSet results = stmt.executeQuery();
				DaoFactory factory = new DaoFactory();
				while(results.next()) {
					King king = new King(results.getInt("id"),
							Bukkit.getPlayer(UUID.fromString(results.getString("name"))),
							results.getString("title"),
							results.getString("kingdom_name"),
							results.getInt("kingdom_level"),
							results.getInt("current_gen"),
							results.getFloat("balance")
					);
					PersistentData.getInstance().kingData.put(results.getString("name"), king);
					king.relations = factory.getRelations().getByKingId(king.getId());
				}
				new DaoFactory().getClaims().getAll();
				factory.getAlliances().getAll();
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
			}
			return PersistentData.getInstance().kingData;
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void save(King king) {
		try {
			PreparedStatement stmt = (PreparedStatement) Data.getInstance().getConnection().prepareStatement(
					"INSERT INTO kings(name, title, kingdom_name, home_chunk, kingdom_level, current_gen, balance) VALUES(?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
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
			PersistentData.getInstance().kingData.put(king.getUuid().toString(), king);
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
