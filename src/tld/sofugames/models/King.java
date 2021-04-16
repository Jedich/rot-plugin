package tld.sofugames.models;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import tld.sofugames.data.Data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class King implements Model {

	public int id;
	public String kingdomName = "Unnamed Kingdom";
	private String title;
	public String fullTitle;
	public Player assignedPlayer;
	public ClaimedChunk homeChunk;
	public int kingdomLevel = 1;
	public float goldBalance = 0;
	public float income;
	public int chunkNumber = 0;
	private int currentGen = 0;
	public BossBar kingdomBar;
	public boolean barSetToCancel;

	public King(int id, Player player, ClaimedChunk homeChunk) {
		this.id = id;
		this.assignedPlayer = player;
		this.homeChunk = homeChunk;
		setBossBar();
	}

	public King(int id, Player player, String title, String kingdomName,
				int kingdomLevel, int currentGen, float goldBalance) {
		this.id = id;
		this.assignedPlayer = player;
		this.title = title;
		this.kingdomName = kingdomName;
		this.kingdomLevel = kingdomLevel;
		this.income = 0;
		this.goldBalance = goldBalance;
		this.currentGen = currentGen;
		if(currentGen != 0) {
			generateGen();
		}
		setBossBar();
	}

	public void setBossBar() {
		kingdomBar = Bukkit.createBossBar(kingdomName, BarColor.PURPLE, BarStyle.SOLID);
		kingdomBar.addPlayer(assignedPlayer);
	}

	public void generateGen() {
		if(currentGen == 1) {
			title = ", " + ChatColor.GOLD + "Father of the Nation";
		} else {
			title = Data.getInstance().titles[new Random().nextInt(Data.getInstance().titles.length)];
		}
		fullTitle = assignedPlayer.getName() + " " + Data.getInstance().getRomanNumber(currentGen) + title;
		try {
			updateInDb(Data.getInstance().getConnection(),
					Stream.of(new Object[][]{{"title", title}, {"current_gen", currentGen}})
							.collect(Collectors.toMap(data -> (String) data[0], data -> data[1])));
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public void changeGen() {
		currentGen++;
		generateGen();
	}

	public float getFee() {
		return (float) ((1.3f * chunkNumber) * Math.log(chunkNumber));
	}

	public void changeIncome(float income) {
		this.income += income;
	}

	public boolean pushToDb(Connection connection) throws SQLException {
		PreparedStatement pstmt = (PreparedStatement) connection.prepareStatement(
				"INSERT INTO kings VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
		pstmt.setInt(1, id);
		pstmt.setString(2, assignedPlayer.getUniqueId().toString());
		pstmt.setString(3, title);
		pstmt.setString(4, kingdomName);
		pstmt.setString(5, homeChunk.chunkId);
		pstmt.setInt(6, kingdomLevel);
		pstmt.setInt(7, currentGen);
		pstmt.setFloat(8, goldBalance);
		pstmt.executeUpdate();
		return true;
	}

	@Override
	public boolean readFromDb(Connection connection) throws SQLException {
		return false;
	}

	public boolean updateInDb(Connection connection, Map<String, Object> params) throws SQLException {
		StringBuilder sql = new StringBuilder("UPDATE kings SET ");
		for(Map.Entry<String, Object> entry : params.entrySet()) {
			sql.append(entry.getKey()).append(" = ");
			if(entry.getValue() instanceof String) {
				sql.append("'").append(entry.getValue()).append("', ");
			} else if(entry.getValue() instanceof Integer || entry.getValue() instanceof Float) {
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
