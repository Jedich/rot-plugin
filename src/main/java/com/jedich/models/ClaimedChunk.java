package com.jedich.models;

import org.bukkit.Chunk;
import com.jedich.rot.ChunkType;

import java.util.HashSet;
import java.util.UUID;

public class ClaimedChunk {
	private int id;
	public UUID owner;
	public String chunkId = "";
	public ChunkType type = ChunkType.Default;
	public Chunk world = null;
	public int x, z;
	public float income = 0;

	public ClaimedChunk(int id, String chunkId, UUID owner, ChunkType type, Chunk world) {
		this.id = id;
		this.owner = owner;
		this.chunkId = chunkId;
		this.type = type;
		this.world = world;
		this.x = world.getX();
		this.z = world.getZ();
	}

	public ClaimedChunk(String chunkId, UUID owner, ChunkType type, Chunk world) {
		this.owner = owner;
		this.chunkId = chunkId;
		this.type = type;
		this.world = world;
		this.x = world.getX();
		this.z = world.getZ();
	}

	public ClaimedChunk() {}

	public HashSet<Chunk> getRelatives() {
		HashSet<Chunk> chunks = new HashSet<>();
		chunks.add(world.getWorld().getChunkAt(x+1, z));
		chunks.add(world.getWorld().getChunkAt(x, z+1));
		chunks.add(world.getWorld().getChunkAt(x-1, z));
		chunks.add(world.getWorld().getChunkAt(x, z-1));
		return chunks;
	}

	public float distance(ClaimedChunk toChunk) {
		return (float)Math.sqrt(Math.pow(toChunk.x - x, 2) + Math.pow(toChunk.z - z, 2));
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}

