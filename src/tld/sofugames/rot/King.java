package tld.sofugames.rot;

import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class King implements Model {

	public int id;
	public UUID nickname;
	public String kingdomName;
	public Player assignedPlayer;
	public ClaimedChunk homeChunk;
	public int kingdomLevel;
	public float charge;
	public int chunkNumber;

	public King(int id, Player player, ClaimedChunk homeChunk) {
		this.id = id;
		this.assignedPlayer = player;
		this.homeChunk = homeChunk;
		nickname = player.getUniqueId();
		kingdomLevel = 1;
		chunkNumber = 1;
	}

	public King(int id, Player player, String kingdomName, ClaimedChunk homeChunk, int kingdomLevel, int chunkNumber) {
		this.id = id;
		this.assignedPlayer = player;
		this.kingdomName = kingdomName;
		this.homeChunk = homeChunk;
		nickname = player.getUniqueId();
		this.kingdomLevel = kingdomLevel;
		this.chunkNumber = chunkNumber;
	}

	public boolean pushToDb(Connection connection) throws SQLException {
		PreparedStatement pstmt = (PreparedStatement) connection.prepareStatement(
				"INSERT INTO kings VALUES(?, ?, ?, ?, ?, ?)");
		pstmt.setInt(1, id);
		pstmt.setString(2, nickname.toString());
		pstmt.setString(3, kingdomName);
		pstmt.setString(4, homeChunk.chunkId);
		pstmt.setInt(5, kingdomLevel);
		pstmt.setInt(6, chunkNumber);
		pstmt.executeUpdate();
		return true;
	}

	public boolean updateInDb(Connection connection, String[] params) throws SQLException {
		StringBuilder sql = new StringBuilder("UPDATE kings SET ");
		for(String param:params) {
			sql.append(param).append(" = ?, ");
		}
		sql.deleteCharAt(sql.length() - 2);
		sql.append("WHERE id = ").append(id);
		System.out.println(sql);
		PreparedStatement pstmt = (PreparedStatement) connection.prepareStatement(sql.toString());
		pstmt.setString(1, kingdomName);
		pstmt.executeUpdate();
		return true;
	}
}
