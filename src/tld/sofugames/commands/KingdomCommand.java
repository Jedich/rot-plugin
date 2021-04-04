package tld.sofugames.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tld.sofugames.data.Data;
import tld.sofugames.models.ClaimedChunk;
import tld.sofugames.models.King;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static org.bukkit.Bukkit.getServer;

public class KingdomCommand implements CommandExecutor {
	Connection connection;
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("kingdom")) {
			Player player = (Player) sender;
			String UUID = player.getUniqueId().toString();
			String chunkName = player.getLocation().getChunk().toString();
			if (args[0].equalsIgnoreCase("setname")) {
				try {
					Data.getInstance().kingData.get(UUID).kingdomName = args[1];
					Data.getInstance().kingData.get(UUID).updateInDb(connection, new String[]{"kingdom_name"});
				} catch (SQLException e) {
					sender.sendMessage("Update execution error");
					e.printStackTrace();
				}
			} else if (args[0].equalsIgnoreCase("info")) {

				if (!Data.getInstance().kingData.containsKey(UUID)) {
					sender.sendMessage("You are not a king yet! Type your first /claim to become a king!");
				} else {
					King thisKing = Data.getInstance().kingData.get(UUID);
					sender.sendMessage((ChatColor.GOLD + thisKing.kingdomName) +
							ChatColor.WHITE + ", the kingdom of " + ChatColor.GOLD + sender.getName());
					sender.sendMessage("Kingdom level: " + thisKing.kingdomLevel);
					sender.sendMessage("Total chunks: " + thisKing.chunkNumber);
					if (chunkName.equals(thisKing.homeChunk.chunkId)) {
						sender.sendMessage("This is the ruler's home chunk.");
					}
				}
			} else if (args[0].equalsIgnoreCase("show")) {
				if (!Data.getInstance().kingData.containsKey(UUID)) {
					sender.sendMessage("You are not a king yet! Type your first /claim to become a king!");
				} else {
					for (Map.Entry<String, ClaimedChunk> chunk : Data.getInstance().claimData.entrySet()) {
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
		}
		return false;
	}
}
