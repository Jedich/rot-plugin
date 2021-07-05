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
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class King extends RotPlayer implements Model {

	public int id;

	public String kingdomName = "Unnamed Kingdom";
	private String title;
	public String fullTitle;
	public int kingdomLevel = 1;
	private float goldBalance = 10;
	private float income;
	private int chunkNumber = 0;
	private int currentGen = 0;
	private BossBar kingdomBar;
	public boolean barSetToCancel;
	public LinkedList<ClaimedChunk> warClaims = new LinkedList<>();
	public HashSet<UUID> hasClaimsOn = new HashSet<>();
	public HashMap<UUID, Integer> relations = new HashMap<>();
	public HashSet<UUID> advisors = new HashSet<>();


	public King(int id, Player player, ClaimedChunk homeChunk) {
		super(player, homeChunk);
		this.id = id;
		setBossBar();
	}

	public King(int id, Player player, String title, String kingdomName,
				int kingdomLevel, int currentGen, float goldBalance) {
		super(player, null);
		this.id = id;
		this.assignedPlayer = player;
		this.title = title;
		this.kingdomName = kingdomName;
		this.kingdomLevel = kingdomLevel;
		this.setIncome(0);
		this.goldBalance = goldBalance;
		this.setCurrentGen(currentGen);
		if(player != null) {
			loadGen();
			setBossBar();
			this.uuid = player.getUniqueId();
		}
	}

	public float getGoldBalance() {
		return goldBalance;
	}

	public void setGoldBalance(float valueToAdd) {
		goldBalance += valueToAdd;
		try {
			updateInDb(Data.getInstance().getConnection(), Collections.singletonMap("balance", getGoldBalance()));
		} catch(SQLException throwables) {
			throwables.printStackTrace();
		}
	}

	public void setBossBar() {
		setKingdomBar(Bukkit.createBossBar(kingdomName, BarColor.PURPLE, BarStyle.SOLID));
		getKingdomBar().addPlayer(assignedPlayer);
	}

	public void generateGen() {
		if(getCurrentGen() == 1) {
			title = ", " + ChatColor.GOLD + "Father of the Nation";
		} else {
			title = Data.getInstance().titles[new Random().nextInt(Data.getInstance().titles.length)];
		}
		fullTitle = assignedPlayer.getName() + " " + Data.getInstance().getRomanNumber(getCurrentGen()) + title + ChatColor.WHITE;
		assignedPlayer.setDisplayName(fullTitle);
		try {
			updateInDb(Data.getInstance().getConnection(),
					Stream.of(new Object[][]{{"title", title}, {"current_gen", getCurrentGen()}})
							.collect(Collectors.toMap(data -> (String) data[0], data -> data[1])));
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public void loadGen() {
		if(!title.equals("")) {
			fullTitle = assignedPlayer.getName() + " " + Data.getInstance().getRomanNumber(getCurrentGen()) + title;
		} else {
			generateGen();
		}
		assignedPlayer.setDisplayName(fullTitle);
	}

	public void changeGen() {
		setCurrentGen(getCurrentGen() + 1);
		generateGen();
		assignedPlayer.setDisplayName(fullTitle);
	}

	public void addWarClaim(ClaimedChunk chunk) throws SQLException {
		PreparedStatement pstmt = (PreparedStatement) Data.getInstance().getConnection().prepareStatement(
				"INSERT INTO war_claims(by_king, chunk_name) VALUES(?, ?)", Statement.RETURN_GENERATED_KEYS);
		pstmt.setString(1, getUuid().toString());
		pstmt.setString(2, chunk.world.toString());
		pstmt.executeUpdate();
		warClaims.add(chunk);
		hasClaimsOn.add(chunk.owner);
	}

	public void deleteWarClaim(ClaimedChunk chunk) throws SQLException {
		PreparedStatement pstmt = (PreparedStatement) Data.getInstance().getConnection().prepareStatement(
				"DELETE FROM war_claims WHERE by_king = ? AND chunk_name = ?");
		pstmt.setString(1, getUuid().toString());
		pstmt.setString(2, chunk.world.toString());
		pstmt.executeUpdate();
		warClaims.remove(chunk);
		hasClaimsOn.remove(chunk.owner);
	}

	public void contact(King other) {
		if(!relations.containsKey(other.getUuid()) && other.assignedPlayer != null && !other.equals(this)) {
			if(other.assignedPlayer.isOnline()) {
				relations.put(other.getUuid(), 50);
				other.relations.put(getUuid(), 50);
				assignedPlayer.sendMessage("First contact: " + kingdomName + " and " + other.kingdomName);
				other.assignedPlayer.sendMessage("First contact: " + kingdomName + " and " + other.kingdomName);
				try {
					setRelationsDb(this, other);
					setRelationsDb(other, this);
				} catch(SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void setRelationsDb(King thisKing, King other) throws SQLException {
		PreparedStatement pstmt = (PreparedStatement) Data.getInstance().getConnection().prepareStatement(
				"INSERT INTO relations(name, meaning_of, value) VALUES(?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
		pstmt.setString(1, thisKing.getUuid().toString());
		pstmt.setString(2, other.getUuid().toString());
		pstmt.setInt(3, thisKing.relations.get(other.getUuid()));
		pstmt.executeUpdate();
	}

	private void updateRelationsInDb(King thisKing, King other) throws SQLException {
		PreparedStatement pstmt = (PreparedStatement) Data.getInstance().getConnection().prepareStatement(
				"UPDATE relations SET value=? WHERE name=? AND meaning_of=?");
		pstmt.setInt(1, thisKing.relations.get(other.getUuid()));
		pstmt.setString(2, thisKing.getUuid().toString());
		pstmt.setString(3, other.getUuid().toString());
		pstmt.executeUpdate();
	}

	public void changeMeaning(UUID with, int value) {
		relations.put(with, relations.get(with) + value);
		if(relations.get(with) > 100) {
			relations.put(with, 100);
		}
		if(relations.get(with) < -100) {
			relations.put(with, -100);
		}
		try {
			updateRelationsInDb(this, Data.getInstance().kingData.get(with.toString()));
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public float getFee() {
		return (float) ((1.3f * (getChunkNumber() + 0.5)) * Math.log(getChunkNumber() + 0.5));
	}

	public void changeIncome(float income) {
		this.setIncome(this.getIncome() + income);
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
		pstmt.setInt(7, getCurrentGen());
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

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(!(o instanceof King)) return false;
		King king = (King) o;
		return id == king.id &&
				getCurrentGen() == king.getCurrentGen() &&
				Objects.equals(getUuid(), king.getUuid()) &&
				Objects.equals(kingdomName, king.kingdomName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, getUuid(), kingdomName, getCurrentGen());
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public float getIncome() {
		return income;
	}

	public void setIncome(float income) {
		this.income = income;
	}

	public int getChunkNumber() {
		return chunkNumber;
	}

	public void setChunkNumber(int chunkNumber) {
		this.chunkNumber = chunkNumber;
	}

	public int getCurrentGen() {
		return currentGen;
	}

	public void setCurrentGen(int currentGen) {
		this.currentGen = currentGen;
	}

	public BossBar getKingdomBar() {
		return kingdomBar;
	}

	public void setKingdomBar(BossBar kingdomBar) {
		this.kingdomBar = kingdomBar;
	}

	public boolean isAtWar() {
		return atWar;
	}

	public void setAtWar(boolean atWar) {
		this.atWar = atWar;
	}

	public War getCurrentWar() {
		return currentWar;
	}

	public void setCurrentWar(War currentWar) {
		this.currentWar = currentWar;
	}


}
