package tld.sofugames.data;

import org.bukkit.Bukkit;
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
	public Optional<ClaimedChunk> get(String query) {
		return Optional.of(getAll().get(query));
	}

	@Override
	public Map<String, ClaimedChunk> getAll() {
		try {
			if(PersistentData.getInstance().kingData.size() == 0) {
				PreparedStatement stmt = (PreparedStatement) connection.prepareStatement("SELECT * FROM user_claims");
				ResultSet results = stmt.executeQuery();
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
					owner.setChunkNumber(owner.getChunkNumber() + 1);
					Data.getInstance().lastClaim = results.getInt("id");
				}
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void save(ClaimedChunk chunk) {
		try {
			PreparedStatement pstmt = Data.getInstance().getConnection().prepareStatement(
					"INSERT INTO user_claims VALUES(?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, chunk.chunkId);
			pstmt.setInt(2, chunk.world.getX());
			pstmt.setInt(3, chunk.world.getZ());
			pstmt.setString(4, chunk.owner.toString());
			pstmt.setString(5, chunk.type.name());
			pstmt.executeUpdate();
			System.out.println(pstmt.toString());
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
			pstmt.setInt(1, chunk.id);
			pstmt.executeUpdate();
			getAll().remove(chunk.world.toString());
			King owner = kingData.get(chunk.owner.toString());
			owner.changeChunkNumber(-1);
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
}
