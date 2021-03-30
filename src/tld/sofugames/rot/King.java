package tld.sofugames.rot;

import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class King implements Model {

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

	public King(Player player, String kingdomName, ClaimedChunk homeChunk, int kingdomLevel, int chunkNumber) {
		this.assignedPlayer = player;
		this.kingdomName = kingdomName;
		this.homeChunk = homeChunk;
		nickname = player.getDisplayName();
		this.kingdomLevel = kingdomLevel;
		this.chunkNumber = chunkNumber;
	}

	public boolean pushToDb(Connection connection) throws SQLException {
		PreparedStatement pstmt = (PreparedStatement) connection.prepareStatement(
				"INSERT INTO kings VALUES(?, ?, ?, ?, ?)");
		pstmt.setString(2, nickname);
		pstmt.setString(3, kingdomName);
		pstmt.setString(4, homeChunk.chunkId);
		pstmt.setInt(5, kingdomLevel);
		pstmt.setInt(6, chunkNumber);
		pstmt.executeUpdate();
		return true;
	}
}
