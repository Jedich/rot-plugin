package tld.sofugames.rot;

import com.mysql.jdbc.*;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

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
	HashMap<String, ClaimedChunk> claimData = new HashMap<>();
	HashMap<String, King> kingData = new HashMap<>();
	int lastClaim = 1, lastKing = 1;

	public int getLastClaim() {
		return lastClaim++;
	}

	public int getLastKing() {
		return lastKing++;
	}

	@Override
	public void onEnable() {
		//TODO: db tables and checking for new data
		try {
			connection = (Connection) DriverManager.getConnection(url, username, password);
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
				claimData.put(results.getString("name"),
						new ClaimedChunk(results.getInt("id"),
								results.getString("name"),
								results.getString("owner"),
								ChunkType.valueOf(results.getString("type")),
								world.getChunkAt(results.getInt("chunk_x"),
										results.getInt("chunk_y"))));
				lastClaim++;
			}
			stmt = (PreparedStatement) connection.prepareStatement("SELECT * FROM kings");
			results = stmt.executeQuery();
			while (results.next()) {
				kingData.put(results.getString("name"), new King(results.getInt("id"),
						Objects.requireNonNull(getServer().getPlayer(results.getString("name"))),
						results.getString("kingdom_name"),
						claimData.get(results.getString("home_chunk")),
						results.getInt("kingdom_level"),
						results.getInt("chunk_number")
				));
				lastKing++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("claim")) {
			Player player = getServer().getPlayer(sender.getName());
			String chunkName = player.getLocation().getChunk().toString();
			if (!claimData.containsKey(chunkName)) {
				if (!kingData.containsKey(sender.getName())) {

					ClaimedChunk homeChunk = new ClaimedChunk(getLastClaim(), chunkName,
							sender.getName(), ChunkType.Home, player.getLocation().getChunk());
					try {
						homeChunk.pushToDb(connection);
					} catch (SQLException e) {
						e.printStackTrace();
						return false;
					}
					sender.sendMessage("Let your journey begin here.");
					claimData.put(player.getLocation().getChunk().toString(), homeChunk);
					King thisKing = new King(getLastKing(), player, homeChunk);
					kingData.put(sender.getName(), thisKing);
					try {
						if (thisKing.pushToDb(connection)) {
							sender.sendMessage("Chunk successfully claimed!" + ChatColor.GOLD + " You are now a King.");
							sender.sendMessage("Please, name your kingdom with /kingdom setname [NAME]");
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				} else {
					//TODO: Check for neighboring chunks
					sender.sendMessage("Chunk successfully claimed!");
					ClaimedChunk claim = new ClaimedChunk(getLastClaim(), chunkName,
							sender.getName(), ChunkType.Default, player.getLocation().getChunk());
					claimData.put(player.getLocation().getChunk().toString(), claim);
					try {
						claim.pushToDb(connection);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Chunk is already claimed.");
			}
			return true;
		} else if (command.getName().equalsIgnoreCase("kingdom")) {
			if (args[0].equalsIgnoreCase("setname")) {
				try {
					kingData.get(sender.getName()).kingdomName = args[1];
					kingData.get(sender.getName()).updateInDb(connection, new String[]{"kingdom_name"});
				} catch (SQLException e) {
					sender.sendMessage("Update execution error");
					e.printStackTrace();
				}
			} else if (args[0].equalsIgnoreCase("info")) {
				Player player = getServer().getPlayer(sender.getName());
				String chunkName = player.getLocation().getChunk().toString();
				if (!kingData.containsKey(sender.getName())) {
					sender.sendMessage("You are not a king yet! Type your first /claim to become a king!");
				} else {
					King thisKing = kingData.get(sender.getName());
					sender.sendMessage((ChatColor.GOLD + thisKing.kingdomName) +
							ChatColor.WHITE + ", the kingdom of " + ChatColor.GOLD + sender.getName());
					sender.sendMessage("Kingdom level: " + thisKing.kingdomLevel);
					sender.sendMessage("Total chunks: " + thisKing.chunkNumber);
					if (chunkName.equals(thisKing.homeChunk.chunkId)) {
						sender.sendMessage("This is the ruler's home chunk.");
					}
				}
			} else if (args[0].equalsIgnoreCase("show")) {
				if (!kingData.containsKey(sender.getName())) {
					sender.sendMessage("You are not a king yet! Type your first /claim to become a king!");
				} else {
					Player player = getServer().getPlayer(sender.getName());
					String chunkName = player.getLocation().getChunk().toString();
					for (Map.Entry<String, ClaimedChunk> chunk : claimData.entrySet()) {
						ClaimedChunk ch = chunk.getValue();
						int a = ch.world.getX() * 16;
						int b = ch.world.getZ() * 16;
						if (chunk.getValue().owner.equals(sender.getName())) {
							for (int x = 0; x < 16; x++) {
								for (int z = 0; z < 16; z++) {
									World world = player.getWorld();
									player.spawnParticle(Particle.VILLAGER_HAPPY,
											new Location(world, a + x + 0.5f, world.getHighestBlockAt(a + x, b + z).getY()+1, b + z + 0.5f),
											1, 0, 0, 0);
								}
							}
						}
					}
				}
			} else {
				sender.sendMessage("/kingdom setname [NAME], info");
			}
			return true;
		} else if (command.getName().equalsIgnoreCase("reset")) {
			claimData.clear();
			kingData.clear();
			onDisable();
			onEnable();
			return true;
		} else if (command.getName().equalsIgnoreCase("chunk-info")) {
			Player player = getServer().getPlayer(sender.getName());
			String chunkName = player.getLocation().getChunk().toString();
			String ownerName = "None";
			sender.sendMessage("Current chunk: " + chunkName);
			if (claimData.get(chunkName) != null) {
				sender.sendMessage("Current owner: " + claimData.get(chunkName).owner);
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
}
