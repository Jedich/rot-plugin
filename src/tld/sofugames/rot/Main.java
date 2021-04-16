package tld.sofugames.rot;

import com.mysql.jdbc.*;
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

	private File customConfigFile;
	private FileConfiguration customConfig;

	public FileConfiguration getCustomConfig() {
		return this.customConfig;
	}

	private void createCustomConfig() {
		customConfigFile = new File(getDataFolder(), "db.yml");
		if (!customConfigFile.exists()) {
			customConfigFile.getParentFile().mkdirs();
			saveResource("db.yml", false);
		}

		customConfig = new YamlConfiguration();
		try {
			customConfig.load(customConfigFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onEnable() {
		createCustomConfig();
		Data data = Data.getInstance();
		data.username = getCustomConfig().getString("database.username");
		data.password = getCustomConfig().getString("database.password");
		data.host = getCustomConfig().getString("database.host");
		data.port = getCustomConfig().getString("database.port");
		data.db = getCustomConfig().getString("database.db");
		if(data.host != null) {
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
					Data.getInstance().houseData.put(results.getString("bed_block"), new House(results.getInt("id"),
							UUID.fromString(results.getString("owner")),
							results.getString("bed_block"),
							results.getInt("area"),
							results.getInt("benefits"),
							results.getFloat("income")
					));
					Data.getInstance().lastHouse = results.getInt("id");
					Data.getInstance().kingData.get(results.getString("owner")).changeIncome(results.getFloat("income"));
				}
			} catch(SQLException e) {
				e.printStackTrace();
			}

			getCommand("claim").setExecutor(new ClaimCommand());
			getCommand("unclaim").setExecutor(new UnclaimCommand());
			getCommand("kingdom").setExecutor(new KingdomCommand());

			getServer().getPluginManager().registerEvents(new EventListener(), this);
			getServer().getPluginManager().registerEvents(new MultiBlockPlaceListener(), this);
			getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);

			Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "time set 0");
			BukkitScheduler scheduler = getServer().getScheduler();
			scheduler.scheduleSyncRepeatingTask(this, this::checkIncomes, 0L, 24000L);
		}
		else {
			System.out.println("Database not configured! Restarting...");
			restart();
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("reset")) {
			restart();
			return true;
		} else if (command.getName().equalsIgnoreCase("chunk-info")) {
			Player player = getServer().getPlayer(sender.getName());
			String chunkName = player.getLocation().getChunk().toString();
			String ownerName = "None";
			sender.sendMessage("Current chunk: " + chunkName);
			if (Data.getInstance().claimData.get(chunkName) != null) {
				sender.sendMessage("Current owner: " + Data.getInstance().claimData.get(chunkName).owner);
			} else {
				sender.sendMessage("The chunk is available for conquest!");
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
			getCustomConfig().save("db.yml");
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		HandlerList.unregisterAll();
		getServer().getScheduler().cancelTasks(this);
	}

	public void checkIncomes() {
		for (King king : Data.getInstance().kingData.values()) {
			if (king.assignedPlayer != null) {
				if (Bukkit.getPlayerExact(king.assignedPlayer.getName()) != null) {
					try {
						king.goldBalance += (king.income - king.getFee());
						king.updateInDb(Data.getInstance().getConnection(), Collections.singletonMap("balance", king.goldBalance));

						king.assignedPlayer.sendMessage(ChatColor.GOLD + "Good morning, my honor!");
						king.assignedPlayer.sendMessage("Income: " + ChatColor.GREEN + String.format(Locale.US, "%.1f", king.income) + "ing. " +
								ChatColor.WHITE + "Charge: " + ChatColor.RED + String.format(Locale.US, "%.1f", king.getFee()) + "ing.");
						king.assignedPlayer.sendMessage("Your balance: " + ChatColor.GOLD + String.format(Locale.US, "%.1f", king.goldBalance) + "ing.");
						if (king.goldBalance < -5) {
							king.assignedPlayer.sendMessage(ChatColor.DARK_RED + "The treasury is empty, my lord! " +
									"We should take a foreign aid before it's not too late!");
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}