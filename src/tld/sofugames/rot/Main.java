package tld.sofugames.rot;

import com.mysql.jdbc.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.*;
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

	final String username="root"; // Enter in your db username
	final String password=""; // Enter your password for the db
	final String url = "jdbc:mysql://localhost:3306/rotr"; // Enter URL with db name

	//Connection vars
	static Connection connection; //This is the variable we will use to connect to database
	HashMap<String, ClaimedChunk> claimData = new HashMap<>();
	HashMap<String, King> kingData = new HashMap<>();

	@Override
	public void onEnable() {
		//TODO: db tables and checking for new data
		try {
			connection = (Connection) DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ResultSet results;
		World world = Bukkit.getOnlinePlayers().iterator().next().getWorld();
		try {
			PreparedStatement stmt = (PreparedStatement) connection.prepareStatement("SELECT * FROM user_claims");
			results = stmt.executeQuery();
			while (results.next()) {
				claimData.put(results.getString("name"),
						new ClaimedChunk(results.getString("name"),
								results.getString("owner"),
								ChunkType.valueOf(results.getString("type")),
								world.getChunkAt(results.getInt("chunk_x"),
										results.getInt("chunk_y"))));
			}
			stmt = (PreparedStatement) connection.prepareStatement("SELECT * FROM kings");
			results = stmt.executeQuery();
			while (results.next()) {
				kingData.put(results.getString("name"), new King(
						Objects.requireNonNull(getServer().getPlayer(results.getString("name"))),
						claimData.get(results.getString("home_chunk")),
						results.getInt("kingdom_level"),
						results.getInt("chunk_number")
				));
			}
		}
		catch (SQLException e) {
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
					sender.sendMessage("Let your journey begin here.");
					ClaimedChunk homeChunk = new ClaimedChunk(chunkName,
							sender.getName(), ChunkType.Home, player.getLocation().getChunk());

					claimData.put(player.getLocation().getChunk().toString(), homeChunk);
					kingData.put(sender.getName(), new King(player, homeChunk));

					sender.sendMessage("Chunk successfully claimed!" + ChatColor.GOLD + " You are now a King.");
					sender.sendMessage("Please, name your kingdom with /kingdom setname [NAME]");
				}
				else {
					//TODO: Check for neighboring chunks
					sender.sendMessage("Chunk successfully claimed!");
					claimData.put(player.getLocation().getChunk().toString(), new ClaimedChunk(chunkName,
							sender.getName(), ChunkType.Default, player.getLocation().getChunk()));
				}
			}
			else {
				sender.sendMessage(ChatColor.RED + "Chunk is already claimed.");
			}
			return true;
		}
		else if(command.getName().equalsIgnoreCase("kingdom")) {
			if(args[0].equalsIgnoreCase("name")) {
				kingData.get(sender.getName()).kingdomName = args[1];
			}
			else if(args[0].equalsIgnoreCase("info")) {
				Player player = getServer().getPlayer(sender.getName());
				String chunkName = player.getLocation().getChunk().toString();
				if(!kingData.containsKey(sender.getName())) {
					sender.sendMessage("You are not a king yet! Type your first /claim to become a king!");
				} else {
					King thisKing = kingData.get(sender.getName());
					sender.sendMessage(thisKing.kingdomName + ", the kingdom of " + sender.getName());
					sender.sendMessage("Ruler level: " + thisKing.kingdomLevel);
					sender.sendMessage("Total chunks: " + thisKing.chunkNumber);
					if(chunkName.equals(thisKing.homeChunk.chunkId)) {
						sender.sendMessage("This is the ruler's home chunk.");
					}
				}
			}
		}
		else if(command.getName().equalsIgnoreCase("reset")) {
			claimData.clear();
			return true;
		}
		else if(command.getName().equalsIgnoreCase("chunk-info")) {
			Player player = getServer().getPlayer(sender.getName());
			String chunkName = player.getLocation().getChunk().toString();
			String ownerName = "None";
			sender.sendMessage("Current chunk: " + chunkName);
			if(claimData.get(chunkName) != null) {
				sender.sendMessage("Current owner: " + claimData.get(chunkName).owner);
			}
			else {
				sender.sendMessage("The chunk is available for conquer!");
			}
			return true;
		}
		return false;
	}

//	public void sendToDatabase() throws SQLException {
//		connection.setAutoCommit(false);
//		PreparedStatement pstmt = null;
//		//pstmt = (PreparedStatement) connection.prepareStatement(
//		//		"INSERT INTO user_claims VALUES(?, ?)");
//		//for(Map.Entry<String, String> entry : claimsModel.entrySet()) {
//		//	pstmt.setString(1, entry.getKey());
//		//	pstmt.setString(2, entry.getValue());
//		//	pstmt.addBatch();
//		//}
//		pstmt.executeBatch();
//		connection.commit();
//		connection.setAutoCommit(true);
//	}

	@Override
	public void onDisable() {
		//TODO: check for data uniqueness
	}
//		try {
//			sendToDatabase();
//		}
//		catch (SQLException e) {
//			e.printStackTrace();
//		}
//		// invoke on disable.
//		try { // using a try catch to catch connection errors (like wrong sql password...)
//			if (connection!=null && !connection.isClosed()){ // checking if connection isn't null to
//				// avoid receiving a nullpointer
//				connection.close(); // closing the connection field variable.
//			}
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//	}
}
