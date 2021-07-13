package tld.sofugames.dao.impl;

import org.bukkit.Bukkit;
import tld.sofugames.dao.Dao;
import tld.sofugames.data.Data;
import tld.sofugames.dao.impl.PersistentData;
import tld.sofugames.models.ClaimedChunk;
import tld.sofugames.models.King;
import tld.sofugames.rot.ChunkType;

import java.sql.*;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ClaimDao extends PersistentData implements Dao<ClaimedChunk> {

	Connection connection;

	public ClaimDao() {
		connection = Data.getInstance().getConnection();
	}

	@Override
	public Map<String, ClaimedChunk> getAll() {
		try {
			if(PersistentData.getInstance().claimData.size() == 0) {
				Statement pstmt = connection.createStatement();
				ResultSet results = pstmt.executeQuery("SELECT CASE WHEN EXISTS(SELECT 1 FROM user_claims) THEN 0 ELSE 1 END AS IsEmpty");
				if(!results.next()) {
					return null;
				}
				PreparedStatement stmt = (PreparedStatement) connection.prepareStatement("SELECT * FROM user_claims");
				results = stmt.executeQuery();
				while(results.next()) {
					ClaimedChunk newChunk = new ClaimedChunk(results.getInt("id"),
							results.getString("name"),
							UUID.fromString(results.getString("owner")),
							ChunkType.valueOf(results.getString("type")),
							Bukkit.getWorlds().get(0).getChunkAt(results.getInt("chunk_x"),
									results.getInt("chunk_y")));

					PersistentData.getInstance().claimData.put(results.getString("name"), newChunk);
					King owner = PersistentData.getInstance().kingData.get(newChunk.owner.toString());
					if(newChunk.type == ChunkType.Home) {
						owner.homeChunk = newChunk;
					}
					owner.changeChunkNumber(1);
				}
			}
			return PersistentData.getInstance().claimData;
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void save(ClaimedChunk chunk) {
		try {
			PreparedStatement pstmt = Data.getInstance().getConnection().prepareStatement(
					"INSERT INTO user_claims(name, chunk_x, chunk_y, owner, type) VALUES(?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, chunk.chunkId);
			pstmt.setInt(2, chunk.world.getX());
			pstmt.setInt(3, chunk.world.getZ());
			pstmt.setString(4, chunk.owner.toString());
			pstmt.setString(5, chunk.type.name());
			pstmt.executeUpdate();
			PersistentData.getInstance().claimData.put(chunk.chunkId, chunk);
			System.out.println(pstmt.toString());
			ResultSet retrievedId = pstmt.getGeneratedKeys();
			if(retrievedId.next()){
				chunk.setId(retrievedId.getInt(1));
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(ClaimedChunk king, Map<String, Object> params) {

	}

	@Override
	public void delete(ClaimedChunk chunk) {
		try {
			PreparedStatement pstmt = Data.getInstance().getConnection().prepareStatement("DELETE FROM user_claims WHERE id = ?");
			pstmt.setInt(1, chunk.getId());
			pstmt.executeUpdate();
			getAll().remove(chunk.world.toString());
			King owner = kingData.get(chunk.owner.toString());
			owner.changeChunkNumber(-1);
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
}
