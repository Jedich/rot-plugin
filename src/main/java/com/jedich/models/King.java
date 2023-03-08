package com.jedich.models;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import com.jedich.dao.impl.DaoFactory;
import com.jedich.data.Data;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class King extends RotPlayer {

	private int id;

	public UUID rawName;
	public String kingdomName = "Unnamed Kingdom";
	private String title = ", §6Father of the Nation§f";
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
	public HashSet<King> allies = new HashSet<>();
	DaoFactory factory = new DaoFactory();

	public King(Player player, ClaimedChunk homeChunk) {
		super(player, homeChunk);
		if(player != null) {
			setBossBar();
		}
	}

	public King() {
	}

	public King(int id, UUID rawName, Player player, String title, String kingdomName,
				int kingdomLevel, int currentGen, float goldBalance) {
		super(player, null);
		this.id = id;
		this.rawName = rawName;
		this.assignedPlayer = player;
		this.title = title;
		this.kingdomName = kingdomName;
		this.kingdomLevel = kingdomLevel;
		this.setIncome(0);
		this.goldBalance = goldBalance;

		this.setCurrentGen(currentGen);
		if(player != null) {
			setBossBar();
			this.uuid = player.getUniqueId();
		}
	}

	public float getGoldBalance() {
		return goldBalance;
	}

	public void setGoldBalance(float valueToAdd) {
		goldBalance += valueToAdd;
		new DaoFactory().getKings().update(this, Collections.singletonMap("balance", getGoldBalance()));
	}

	public void setBossBar() {
		setKingdomBar(Bukkit.createBossBar(kingdomName, BarColor.PURPLE, BarStyle.SOLID));
		getKingdomBar().addPlayer(assignedPlayer);
	}

	public String getFullTitle() {
		return assignedPlayer.getName() + " " + Data.getInstance().getRomanNumber(getCurrentGen()) + title + ChatColor.WHITE;
	}

	public void setTitle(String title) {
		this.title = ", " + ChatColor.GOLD + title;
		assignedPlayer.setDisplayName(getFullTitle());
	}

	public void generateGen() {
		if(getCurrentGen() != 1) {
			title = Data.getInstance().titles[new Random().nextInt(Data.getInstance().titles.length)];
		}
		assignedPlayer.setDisplayName(getFullTitle());
		new DaoFactory().getKings().update(this,
				Stream.of(new Object[][]{{"title", title}, {"current_gen", getCurrentGen()}})
						.collect(Collectors.toMap(data -> (String) data[0], data -> data[1])));
	}

	public void changeGen() {
		setCurrentGen(getCurrentGen() + 1);
		generateGen();
		assignedPlayer.setDisplayName(getFullTitle());
	}

	public void addWarClaim(ClaimedChunk chunk) throws SQLException {
		PreparedStatement pstmt = Data.getInstance().getConnection().prepareStatement(
				"INSERT INTO war_claims(by_king, chunk_name) VALUES(?, ?)", Statement.RETURN_GENERATED_KEYS);
		pstmt.setString(1, getUuid().toString());
		pstmt.setString(2, chunk.world.toString());
		pstmt.executeUpdate();
		warClaims.add(chunk);
		hasClaimsOn.add(chunk.owner);
	}

	public void deleteWarClaim(ClaimedChunk chunk) throws SQLException {
		PreparedStatement pstmt = Data.getInstance().getConnection().prepareStatement(
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
				factory.getRelations().save(new Relation(this, other));
			}
		}
	}

	public void addAlly(King ally) throws IllegalArgumentException {
		if(allies.size() > 1 || ally.allies.size() > 1) {
			throw new IllegalArgumentException("one of the sides has too much alliances.");
		}
		allies.add(ally);
		ally.allies.add(this);
		factory.getAlliances().save(new Relation(this, ally));
	}

	public void deleteAlly(King ally) throws IllegalArgumentException {
		allies.remove(ally);
		ally.allies.remove(this);
		factory.getAlliances().delete(new Relation(this, ally));
	}

	public void changeMeaning(UUID with, int value) {
		relations.put(with, relations.get(with) + value);
		if(relations.get(with) > 100) {
			relations.put(with, 100);
		}
		if(relations.get(with) < -100) {
			relations.put(with, -100);
		}
		factory.getRelations().update(new Relation(this, factory.getKings().get(with.toString()).get()),
				Collections.emptyMap());
	}

	public float getFee() {
		return (float) (getChunkNumber() < 26.821 ? 22 * Math.sin(0.1 * getChunkNumber() + 4.7f) + 22.2f : getChunkNumber() + 14.9747);
		//return (float) (getChunkNumber() < 25 ? 22 * Math.sin(0.1 * getChunkNumber() + 4.7f) + 22.2f : 2 * Math.log10(getChunkNumber() - 16) * 15 + 11);
		//return (float) Math.pow(getChunkNumber(), 1.9f) / 20 + 0.15f;
		// ((1.3f * (getChunkNumber() + 0.5)) * Math.log(getChunkNumber() + 0.5));
	}

	public void changeIncome(float income) {
		this.setIncome(this.getIncome() + income);
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

	public void changeChunkNumber(int chunkNumber) {
		this.chunkNumber += chunkNumber;
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

	public String getTitle() {
		return title;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
