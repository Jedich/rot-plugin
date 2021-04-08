package tld.sofugames.models;

import org.bukkit.Chunk;
import tld.sofugames.rot.ChunkType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class ClaimedChunk implements Model {
	public int id;
	public UUID owner;
	public String chunkId = "";
	public ChunkType type = ChunkType.Default;
	public Chunk world = null;
	public float income = 0;

	public ClaimedChunk(int id, String chunkId, UUID owner, ChunkType type, Chunk world) {
		this.id = id;
		this.owner = owner;
		this.chunkId = chunkId;
		this.type = type;
		this.world = world;
	}

	@Override
	public boolean pushToDb(Connection connection) throws SQLException {
		PreparedStatement pstmt = (PreparedStatement) connection.prepareStatement(
				"INSERT INTO user_claims VALUES(?, ?, ?, ?, ?, ?)");
		pstmt.setInt(1, id);
		pstmt.setString(2, chunkId);
		pstmt.setInt(3, world.getX());
		pstmt.setInt(4, world.getZ());
		pstmt.setString(5, owner.toString());
		pstmt.setString(6, type.name());
		pstmt.executeUpdate();
		return true;
	}

	@Override
	public String toString() {
		return "ClaimedChunk{" +
				"id=" + id +
				", owner=" + owner +
				", chunkId='" + chunkId + '\'' +
				", type=" + type +
				", world=" + world +
				", income=" + income +
				'}';
	}
}

