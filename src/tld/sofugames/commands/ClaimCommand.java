package tld.sofugames.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tld.sofugames.data.Data;
import tld.sofugames.models.ClaimedChunk;
import tld.sofugames.models.King;
import tld.sofugames.rot.ChunkType;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;

public class ClaimCommand implements CommandExecutor {

//	public ClaimCommand(ClaimCommand instance){
//		plugin = instance;
//	}

	Connection connection;

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("claim")) {
			if (connection == null) {
				connection = Data.getInstance().getConnection();
			}
			Player player = (Player) sender;
			String uuid = player.getUniqueId().toString();
			String chunkName = player.getLocation().getChunk().toString();
			if (!Data.getInstance().claimData.containsKey(chunkName)) {
				if (!Data.getInstance().kingData.containsKey(uuid)) {

					ClaimedChunk homeChunk = new ClaimedChunk(Data.getInstance().getLastClaim(), chunkName,
							((Player) sender).getUniqueId(), ChunkType.Home, player.getLocation().getChunk());
					try {
						homeChunk.pushToDb(connection);
					} catch (SQLException e) {
						e.printStackTrace();
						return false;
					}
					sender.sendMessage("Let your journey begin here.");
					try {
						Data.getInstance().claimData.put(player.getLocation().getChunk().toString(), homeChunk);
						King thisKing = new King(Data.getInstance().getLastKing(), player, homeChunk);
						Data.getInstance().kingData.put(uuid, thisKing);
						if (thisKing.pushToDb(connection)) {
							sender.sendMessage("Chunk successfully claimed!" + ChatColor.GOLD + " You are now a King.");
							sender.sendMessage("Please, name your kingdom with /kingdom setname [NAME]");
							Data.getInstance().giveBed((Player) sender);
						}
					} catch (SQLException e) {
						e.printStackTrace();
						sender.sendMessage(ChatColor.RED + "Internal server error.");
					}
				} else {
					//TODO: Check for neighboring chunks
					King king = Data.getInstance().kingData.get(uuid);
					sender.sendMessage("Chunk successfully claimed!");
					ClaimedChunk claim = new ClaimedChunk(Data.getInstance().getLastClaim(), chunkName,
							((Player) sender).getUniqueId(), ChunkType.Default, player.getLocation().getChunk());
					Data.getInstance().claimData.put(player.getLocation().getChunk().toString(), claim);
					king.chunkNumber++;
					try {
						claim.pushToDb(connection);
						king.updateInDb(connection, Collections.singletonMap("chunk_number", Integer.toString(king.chunkNumber)));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Chunk is already claimed.");
			}
			return true;
		}
		return false;
	}
}
