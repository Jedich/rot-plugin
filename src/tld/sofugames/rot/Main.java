package tld.sofugames.rot;

import com.mysql.jdbc.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import tld.sofugames.commands.*;
import tld.sofugames.data.Data;
import tld.sofugames.models.ClaimedChunk;
import tld.sofugames.models.King;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class Main extends JavaPlugin {

	final String username = "root"; // Enter in your db username
	final String password = ""; // Enter your password for the db
	final String url = "jdbc:mysql://localhost:3306/rotr"; // Enter URL with db name
	//Connection vars
	static Connection connection; //This is the variable we will use to connect to database
	
	@Override
	public void onEnable() {
		//TODO: db tables and checking for new data
		try {
			connection = (Connection) DriverManager.getConnection(url, username, password);
			Data.getInstance().connection = connection;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ResultSet results;
		World world;
		world = Bukkit.getWorlds().get(0);
		try {
			PreparedStatement stmt = (PreparedStatement) connection.prepareStatement("SELECT * FROM user_claims");
			results = stmt.executeQuery();
			while (results.next()) {
				Data.getInstance().claimData.put(results.getString("name"),
						new ClaimedChunk(results.getInt("id"),
								results.getString("name"),
								UUID.fromString(results.getString("owner")),
								ChunkType.valueOf(results.getString("type")),
								world.getChunkAt(results.getInt("chunk_x"),
										results.getInt("chunk_y"))));
				Data.getInstance().lastClaim = results.getInt("id");
			}
			stmt = (PreparedStatement) connection.prepareStatement("SELECT * FROM kings");
			results = stmt.executeQuery();
			while (results.next()) {
				Data.getInstance().kingData.put(results.getString("name"), new King(results.getInt("id"),
						Objects.requireNonNull(Bukkit.getOfflinePlayer(UUID.fromString(results.getString("name"))).getPlayer()),
						results.getString("kingdom_name"),
						Data.getInstance().claimData.get(results.getString("home_chunk")),
						results.getInt("kingdom_level"),
						results.getInt("chunk_number")
				));
				Data.getInstance().lastKing = results.getInt("id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		getCommand("claim").setExecutor(new ClaimCommand());
		getCommand("kingdom").setExecutor(new KingdomCommand());

		getServer().getPluginManager().registerEvents(new EventListener(), this);

		Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "/time set 0");
		BukkitScheduler scheduler = getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, this::checkIncomes, 0L, 24000L);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("reset")) {
			Data.getInstance().claimData.clear();
			Data.getInstance().kingData.clear();
			onDisable();
			onEnable();
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


	@Override
	public void onDisable() {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void checkIncomes() {
		for(King king : Data.getInstance().kingData.values()) {
			if(Bukkit.getPlayerExact(king.nickname) != null) {
				king.calculateFee();
				king.goldBalance += Math.round(king.income) - Math.round(king.fee);

				king.assignedPlayer.sendMessage(ChatColor.GOLD + "Good morning, my honor!");
				king.assignedPlayer.sendMessage("Income: " + ChatColor.GREEN + Math.round(king.income) + "ing. " +
						ChatColor.WHITE + "Charge: " + ChatColor.RED + Math.round(king.fee) + "ing.");
				king.assignedPlayer.sendMessage("Your balance: " + ChatColor.GOLD + king.goldBalance + "ing.");
				if(king.goldBalance < -5) {
					king.assignedPlayer.sendMessage(ChatColor.DARK_RED + "The treasury is empty, my lord! " +
							"We should take a foreign aid before it's not too late!");
				}
			}
		}
	}
}
