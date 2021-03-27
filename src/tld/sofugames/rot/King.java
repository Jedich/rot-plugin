package tld.sofugames.rot;

import org.bukkit.entity.Player;

public class King {
	public String nickname;
	public String kingdomName;
	public Player assignedPlayer;
	public ClaimedChunk homeChunk;
	public int kingdomLevel;
	public float charge;
	public int chunkNumber;
	public King(Player player, ClaimedChunk homeChunk) {
		this.assignedPlayer = player;
		this.homeChunk = homeChunk;
		nickname = player.getDisplayName();
		kingdomLevel = 1;
		chunkNumber = 1;
	}
}
