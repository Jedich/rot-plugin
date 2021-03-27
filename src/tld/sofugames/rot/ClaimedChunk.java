package tld.sofugames.rot;

import org.bukkit.Chunk;

public class ClaimedChunk {
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
}

