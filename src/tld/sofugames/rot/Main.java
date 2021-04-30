package tld.sofugames.rot;

import java.sql.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import tld.sofugames.commands.*;
import tld.sofugames.data.Data;
import tld.sofugames.listeners.EventListener;
import tld.sofugames.listeners.MultiBlockPlaceListener;
import tld.sofugames.listeners.PlayerMoveListener;
import tld.sofugames.models.ClaimedChunk;
import tld.sofugames.models.House;
import tld.sofugames.models.King;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class Main extends JavaPlugin {

	static Connection connection;

	@Override
	public void onEnable() {
		Data data = Data.getInstance();
		connection = Data.getInstance().getConnection();
		ResultSet results;
		World world;
		world = Bukkit.getWorlds().get(0);
		try {
			PreparedStatement stmt = (PreparedStatement) connection.prepareStatement("SELECT * FROM kings");
			results = stmt.executeQuery();
			while(results.next()) {
				Data.getInstance().kingData.put(results.getString("name"), new King(results.getInt("id"),
						Bukkit.getPlayer(UUID.fromString(results.getString("name"))),
						results.getString("title"),
						results.getString("kingdom_name"),
						results.getInt("kingdom_level"),
						results.getInt("current_gen"),
						results.getFloat("balance")
				));
				Data.getInstance().lastKing = results.getInt("id");
			}
			stmt = (PreparedStatement) connection.prepareStatement("SELECT * FROM user_claims");
			results = stmt.executeQuery();
			while(results.next()) {
				ClaimedChunk newChunk = new ClaimedChunk(results.getInt("id"),
						results.getString("name"),
						UUID.fromString(results.getString("owner")),
						ChunkType.valueOf(results.getString("type")),
						world.getChunkAt(results.getInt("chunk_x"),
								results.getInt("chunk_y")));

				Data.getInstance().claimData.put(results.getString("name"), newChunk);
				King owner = Data.getInstance().kingData.get(newChunk.owner.toString());
				if(newChunk.type == ChunkType.Home) {
					owner.homeChunk = newChunk;
				}
				owner.chunkNumber++;
				Data.getInstance().lastClaim = results.getInt("id");
			}
			stmt = (PreparedStatement) connection.prepareStatement("SELECT * FROM houses");
			results = stmt.executeQuery();
			while(results.next()) {
				House newHouse = new House(results.getInt("id"),
						UUID.fromString(results.getString("owner")),
						results.getString("bed_block"),
						null,
						results.getInt("area"),
						results.getInt("benefits"),
						results.getFloat("income")
				);
				Data.getInstance().houseData.put(results.getString("bed_block"), newHouse);
				Data.getInstance().lastHouse = results.getInt("id");
				Data.getInstance().kingData.get(results.getString("owner")).changeIncome(results.getFloat("income"));
			}
			stmt = (PreparedStatement) connection.prepareStatement("SELECT * FROM house_blocks");
			results = stmt.executeQuery();
			while(results.next()) {
				Data.getInstance().houseData.get(results.getString("name")).bedBlock =
						getServer().getWorlds().get(0).getBlockAt(results.getInt("x"),
								results.getInt("y"), results.getInt("z"));
			}
			stmt = (PreparedStatement) connection.prepareStatement("SELECT * FROM war_claims");
			results = stmt.executeQuery();
			while(results.next()) {
				Data.getInstance().kingData.get(results.getString("by_king")).warClaims.add(
						Data.getInstance().claimData.get(results.getString("chunk_name")));
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}

		getCommand("claim").setExecutor(new ClaimCommand());
		getCommand("unclaim").setExecutor(new UnclaimCommand());
		KingdomCommand k = new KingdomCommand();
		getCommand("kingdom").setExecutor(k);
		getCommand("kingdom").setTabCompleter(k.new PluginTabCompleter());

		getServer().getPluginManager().registerEvents(new EventListener(), this);
		getServer().getPluginManager().registerEvents(new MultiBlockPlaceListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);

		Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "time set 0");
		BukkitScheduler scheduler = getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, this::checkIncomes, 24000L, 24000L);
		scheduler.scheduleSyncRepeatingTask(this, this::checkHouses, 0, 12000L);
		//}
//		else {
//			System.out.println("Database not configured! Restarting...");
//			restart();
//		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("reset")) {
			restart();
			return true;
		} else if(command.getName().equalsIgnoreCase("chunk-info")) {
			Player player = getServer().getPlayer(sender.getName());
			String chunkName = player.getLocation().getChunk().toString();
			String ownerName = "None";
			sender.sendMessage("Current chunk: " + chunkName);
			if(Data.getInstance().claimData.get(chunkName) != null) {
				sender.sendMessage("Current owner: " + Data.getInstance().claimData.get(chunkName).owner);
			} else {
				sender.sendMessage("The chunk is available for conquest!");
			}
			return true;
		} else if(command.getName().equalsIgnoreCase("timeleft")) {
			if(((Player)sender).getGameMode() == GameMode.SPECTATOR) {
				long time = (Data.getInstance().timers.get(((Player) sender).getUniqueId())
						- System.currentTimeMillis())/1000;
				int minutes = 0;
				while(time >= 60) {
					time -= 60;
					minutes++;
				}
				sender.sendMessage(ChatColor.AQUA + "" + minutes + "m " + time + "s left until respawn...");
			} else {
				sender.sendMessage(ChatColor.ITALIC + "You are alive... breathing, at least.");
			}
			return true;
		}
		return false;
	}

	public void restart() {
		Data.getInstance().claimData.clear();
		Data.getInstance().kingData.clear();
		Data.getInstance().houseData.clear();
		onDisable();
		onEnable();
	}


	@Override
	public void onDisable() {
		try {
			if(connection != null && !connection.isClosed()) {
				connection.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		HandlerList.unregisterAll();
		getServer().getScheduler().cancelTasks(this);
	}

	public void checkHouses() {
		int incorrectTotal = 0;
		HashMap<UUID, Integer> incorrectBeds = new HashMap<>();
		Iterator iterator = Data.getInstance().houseData.entrySet().iterator();
		while(iterator.hasNext()) {
			Map.Entry houseSet = (Map.Entry)iterator.next();
			House house = (House) houseSet.getValue();
			if(Data.getInstance().kingData.get(house.owner.toString()).assignedPlayer != null) {
				Player player = Data.getInstance().kingData.get(house.owner.toString()).assignedPlayer;
				if(!player.isOnline()) {
					continue;
				}
			} else {
				continue;
			}
			try {
				if(!house.isEnclosed()) {
					if(incorrectBeds.containsKey(house.owner)) {
						incorrectBeds.put(house.owner, incorrectBeds.get(house.owner) + 1);
					} else {
						incorrectBeds.put(house.owner, 1);
					}
					house.deleteFromDb(Data.getInstance().getConnection());
					house.bedBlock.breakNaturally();
					iterator.remove();
					incorrectTotal++;
				}
			} catch(HousingOutOfBoundsException | SQLException e) {
				e.printStackTrace();
			}
		}
		for(Map.Entry<UUID, Integer> entry : incorrectBeds.entrySet()) {
			if(Data.getInstance().kingData.get(entry.getKey().toString()).assignedPlayer != null) {
				Player player = Data.getInstance().kingData.get(entry.getKey().toString()).assignedPlayer;
				if(player.isOnline()) {
					player.sendMessage(ChatColor.RED + "Found " + entry.getValue() + " incorrect beds! They were destroyed.");
				}
			}
		}
		System.out.println("Found total " + incorrectTotal + " incorrect beds.");
	}

	public void checkIncomes() {
		for(King king : Data.getInstance().kingData.values()) {
			if(king.assignedPlayer != null) {
				if(Bukkit.getPlayerExact(king.assignedPlayer.getName()) != null) {
					try {
						king.goldBalance += (king.income - king.getFee());
						king.updateInDb(Data.getInstance().getConnection(), Collections.singletonMap("balance", king.goldBalance));

						king.assignedPlayer.sendMessage(ChatColor.GOLD + "Good morning, my honor!");
						king.assignedPlayer.sendMessage("Income: " + ChatColor.GREEN + String.format(Locale.US, "%.1f", king.income) + "ing. " +
								ChatColor.WHITE + "Charge: " + ChatColor.RED + String.format(Locale.US, "%.1f", king.getFee()) + "ing.");
						king.assignedPlayer.sendMessage("Your balance: " + ChatColor.GOLD + String.format(Locale.US, "%.1f", king.goldBalance) + "ing.");
						if(king.goldBalance < -5) {
							king.assignedPlayer.sendMessage(ChatColor.DARK_RED + "The treasury is empty, my lord! " +
									"We should take a foreign aid before it's not too late!");
						}
					} catch(SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}