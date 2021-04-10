package tld.sofugames.models;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tld.sofugames.data.Data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class King implements Model {

	public int id;
	public String kingdomName = "Unnamed Kingdom";
	public Player assignedPlayer;
	public ClaimedChunk homeChunk;
	public int kingdomLevel = 1;
	public float goldBalance = 0;
	public float income;
	public int chunkNumber = 0;

	public King(int id, Player player, ClaimedChunk homeChunk) {
		this.id = id;
		this.assignedPlayer = player;
		this.homeChunk = homeChunk;
	}

	public King(int id, Player player, String kingdomName,
				int kingdomLevel, float goldBalance) {
		this.id = id;
		this.assignedPlayer = player;
		this.kingdomName = kingdomName;
		this.kingdomLevel = kingdomLevel;
		this.income = 0;
		this.goldBalance = goldBalance;
	}

	public float getFee() {
		return (float) ((1.3f * chunkNumber) * Math.log(chunkNumber));
	}

	public void changeIncome(float income) {
		this.income += income;
	}

	public boolean pushToDb(Connection connection) throws SQLException {
		PreparedStatement pstmt = (PreparedStatement) connection.prepareStatement(
				"INSERT INTO kings VALUES(?, ?, ?, ?, ?, ?, ?)");
		pstmt.setInt(1, id);
		pstmt.setString(2, assignedPlayer.getUniqueId().toString());
		pstmt.setString(3, kingdomName);
		pstmt.setString(4, homeChunk.chunkId);
		pstmt.setInt(5, kingdomLevel);
		pstmt.setInt(6, chunkNumber);
		pstmt.setFloat(7, goldBalance);
		pstmt.executeUpdate();
		return true;
	}

	public boolean updateInDb(Connection connection, Map<String, Object> params) throws SQLException {
		StringBuilder sql = new StringBuilder("UPDATE kings SET ");
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			sql.append(entry.getKey()).append(" = ");
			if (entry.getValue() instanceof String) {
				sql.append("'").append(entry.getValue()).append("', ");
			} else if (entry.getValue() instanceof Integer || entry.getValue() instanceof Float) {
				sql.append(entry.getValue()).append(", ");
			} else {
				throw new SQLException("Uncaught type.");
			}
		}
		sql.deleteCharAt(sql.length() - 2);
		sql.append("WHERE id = ").append(id);
		System.out.println(sql.toString());
		PreparedStatement pstmt = (PreparedStatement) connection.prepareStatement(sql.toString());
		//pstmt.setString(1, kingdomName);
		pstmt.executeUpdate();
		return true;
	}
}
