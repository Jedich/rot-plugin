package tld.sofugames.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import tld.sofugames.data.Data;
import tld.sofugames.models.ClaimedChunk;
import tld.sofugames.models.King;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.Bukkit.getServer;

public class KingdomCommand implements CommandExecutor {
	Connection connection;

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("kingdom")) {
			Player player = (Player) sender;
			String uuid = player.getUniqueId().toString();
			String chunkName = player.getLocation().getChunk().toString();
			if (connection == null) {
				connection = Data.getInstance().connection;
			}
			if (args[0].equalsIgnoreCase("setname")) {
				String name = StringUtils.join(args, ' ', 1, args.length);
				Pattern regex = Pattern.compile("[$%&+,'\":;=?@#|]");
				if (!regex.matcher(name).find()){
					Data.getInstance().kingData.get(uuid).kingdomName = name;
					try {
						Data.getInstance().kingData.get(uuid).updateInDb(connection, Collections.singletonMap("kingdom_name", name));
					} catch (SQLException e) {
						sender.sendMessage("Database update execution error");
						e.printStackTrace();
					}
					sender.sendMessage(ChatColor.GOLD + "Your kingdom was successfully renamed to '" + name + "'!");
				} else {
					sender.sendMessage(ChatColor.RED + "Specified name has invalid characters!");
				}
			} else if (args[0].equalsIgnoreCase("info")) {

				if (!Data.getInstance().kingData.containsKey(uuid)) {
					sender.sendMessage("You are not a king yet! Type your first /claim to become a king!");
				} else {
					King thisKing = Data.getInstance().kingData.get(uuid);
					sender.sendMessage((ChatColor.GOLD + thisKing.kingdomName) +
							ChatColor.WHITE + ", the kingdom of " + ChatColor.GOLD + sender.getName());
					sender.sendMessage("Kingdom level: " + thisKing.kingdomLevel);
					sender.sendMessage("Total chunks: " + thisKing.chunkNumber);
					if (chunkName.equals(thisKing.homeChunk.chunkId)) {
						sender.sendMessage("This is the ruler's home chunk.");
					}
				}
			} else if (args[0].equalsIgnoreCase("show")) {
				if (!Data.getInstance().kingData.containsKey(uuid)) {
					sender.sendMessage("You are not a king yet! Type your first /claim to become a king!");
				} else {
					for (Map.Entry<String, ClaimedChunk> chunk : Data.getInstance().claimData.entrySet()) {
						ClaimedChunk ch = chunk.getValue();
						int a = ch.world.getX() * 16;
						int b = ch.world.getZ() * 16;
						if (chunk.getValue().owner.toString().equals(uuid)) {
							for (int x = 0; x < 16; x++) {
								for (int z = 0; z < 16; z++) {
									World world = player.getWorld();
									player.spawnParticle(Particle.VILLAGER_HAPPY,
											new Location(world, a + x + 0.5f, world.getHighestBlockAt(a + x, b + z).getY() + 1, b + z + 0.5f),
											1, 0, 0, 0);
								}
							}
						}
					}
				}
			} else {
				sender.sendMessage("/kingdom setname [NAME], info, show");
			}
			return true;
		}
		return false;
	}
}
