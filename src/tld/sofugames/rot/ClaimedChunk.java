package tld.sofugames.rot;

import org.bukkit.Chunk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ClaimedChunk implements Model {
	public String owner;
	public String chunkId = "";
	public ChunkType type = ChunkType.Default;
	public Chunk world = null;
	public float income = 0;

	public ClaimedChunk(String id, String owner, ChunkType type, Chunk world) {
		this.owner = owner;
		this.chunkId = id;
		this.type = type;
		this.world = world;
	}

	@Override
	public boolean pushToDb(Connection connection) throws SQLException {
		PreparedStatement pstmt = (PreparedStatement) connection.prepareStatement(
				"INSERT INTO user_claims VALUES(?, ?, ?, ?, ?)");
		pstmt.setString(2, chunkId);
		pstmt.setInt(3, world.getX());
		pstmt.setInt(4, world.getZ());
		pstmt.setString(5, owner);
		pstmt.setString(6, type.name());
		pstmt.executeUpdate();
		return true;
	}
}

