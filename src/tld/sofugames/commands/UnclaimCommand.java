package tld.sofugames.commands;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tld.sofugames.data.Data;
import tld.sofugames.models.ClaimedChunk;
import tld.sofugames.rot.ChunkType;

import java.sql.SQLException;

public class UnclaimCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("unclaim")) {
			Player player = ((Player) sender);
			if(Data.getInstance().kingData.containsKey(player.getUniqueId().toString())) {
				Chunk targetChunk = player.getLocation().getChunk();
				if(Data.getInstance().claimData.containsKey(targetChunk.toString())) {
					ClaimedChunk claim = Data.getInstance().claimData.get(targetChunk.toString());
					if(claim.owner.equals(player.getUniqueId())) {
						if(claim.type != ChunkType.Home) {
							try {
								claim.delete();
								sender.sendMessage("Chunk unclaimed.");
							} catch(SQLException e) {
								e.printStackTrace();
							}
						} else {
							sender.sendMessage(ChatColor.RED + "Can't unclaim the home chunk!");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "This is not your kingdoms' chunk!");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "This chunk is not claimed.");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "You are not a king to perform this command.");
			}
			return true;
		}
		return false;
	}
}
