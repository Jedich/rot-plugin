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
		this.uuid = assignedPlayer.getUniqueId();
	}

	public UUID getUuid() {
		return uuid;
	}

	public boolean isWarAlly() {
		return warAlly;
	}

	public void setWarAlly(boolean warAlly) {
		this.warAlly = warAlly;
	}
}