package tld.sofugames.models;

import org.bukkit.entity.Player;

import java.util.UUID;

public class RotPlayer {
	protected UUID uuid;
	public ClaimedChunk homeChunk;
	public Player assignedPlayer;
	protected boolean atWar = false, warAlly = false;
	protected War currentWar;

	public RotPlayer(Player player, ClaimedChunk homeChunk) {
		this.assignedPlayer = player;
		this.homeChunk = homeChunk;
		if(player != null) {
			this.uuid = assignedPlayer.getUniqueId();
		}
	}

	public RotPlayer() {}

	public UUID getUuid() {
		return uuid;
	}

	public boolean isWarAlly() {
		return warAlly;
	}

	public void setWarAlly(boolean warAlly) {
		this.warAlly = warAlly;
	}

	public ClaimedChunk getHomeChunk() {
		return homeChunk;
	}

	public void setHomeChunk(ClaimedChunk homeChunk) {
		this.homeChunk = homeChunk;
	}
}
