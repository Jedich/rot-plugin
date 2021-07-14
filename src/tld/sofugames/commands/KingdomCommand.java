package tld.sofugames.commands;

import com.sun.istack.internal.NotNull;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import tld.sofugames.dao.impl.ClaimDao;
import tld.sofugames.dao.impl.DaoFactory;
import tld.sofugames.dao.impl.KingDao;
import tld.sofugames.data.*;
import tld.sofugames.models.ClaimedChunk;
import tld.sofugames.models.King;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class KingdomCommand implements CommandExecutor {

	DaoFactory daoFactory = new DaoFactory();
	KingDao kingData = daoFactory.getKings();
	ClaimDao claimData = daoFactory.getClaims();

	public void sendCommandList(CommandSender sender) {
		sender.sendMessage("§6Kingdom command:");
		sender.sendMessage("§6setname <name>§f: sets a kingdom name (spaces allowed).");
		sender.sendMessage("§6info§f: gives an important information about your kingdom.");
		sender.sendMessage("§6show§f: visualizes claimed chunks.");
		sender.sendMessage("§6invite§f: invites an advisor to your kingdom.");
		sender.sendMessage("§6join§f: lets you join as an advisor.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("kingdom")) {
			if(args == null || args.length == 0) {
				sendCommandList(sender);
				return true;
			}
			Player player = (Player) sender;
			String uuid = player.getUniqueId().toString();
			King thisKing;
			if(!kingData.get(uuid).isPresent()) {
				sender.sendMessage("You are not a king yet! Type your first /claim to become a king!");
				return true;
			}
			thisKing = kingData.get(uuid).get();
			String chunkName = player.getLocation().getChunk().toString();
			if(args[0].equalsIgnoreCase("setname")) {
				String name = StringUtils.join(args, ' ', 1, args.length);
				Pattern regex = Pattern.compile("[$%&+,'\":;=?@#|]");
				if(name.equals("")) {
					sender.sendMessage(ChatColor.RED + "Please specify a name for a kingdom! (Spaces allowed).");
					return true;
				}
				if(!regex.matcher(name).find()) {
					thisKing.kingdomName = name;
					kingData.update(thisKing, Collections.singletonMap("kingdom_name", name));
					sender.sendMessage(ChatColor.GOLD + "Your kingdom was successfully renamed to '" + name + "'!");
				} else {
					sender.sendMessage(ChatColor.RED + "Specified name has invalid characters!");
				}
			} else if(args[0].equalsIgnoreCase("info")) {
				sender.sendMessage((ChatColor.GOLD + thisKing.kingdomName) +
						ChatColor.WHITE + ", the kingdom of " + ChatColor.GOLD + sender.getName());
				//sender.sendMessage("Kingdom level: " + thisKing.kingdomLevel);
				sender.sendMessage("Total chunks: " + thisKing.getChunkNumber());
				if(chunkName.equals(thisKing.homeChunk.chunkId)) {
					sender.sendMessage("This is the ruler's home chunk.");
				}
				sender.sendMessage("Income: " + ChatColor.GREEN + String.format(Locale.US, "%.1f", thisKing.getIncome()) + "ing. " +
						ChatColor.WHITE + "Charge: " + ChatColor.RED + String.format(Locale.US, "%.1f", thisKing.getFee()) + "ing.");
				sender.sendMessage("Your balance: " + ChatColor.GOLD + String.format(Locale.US, "%.1f", thisKing.getGoldBalance()) + "ing.");
				if(thisKing.getGoldBalance() < -5) {
					sender.sendMessage(ChatColor.DARK_RED + "The treasury is empty, my lord! " +
							"We should take a foreign aid before it's not too late!");
				}
			} else if(args[0].equalsIgnoreCase("show")) {
				for(Map.Entry<String, ClaimedChunk> chunk : claimData.getAll().entrySet()) {
					ClaimedChunk ch = chunk.getValue();
					int a = ch.world.getX() * 16;
					int b = ch.world.getZ() * 16;
					if(chunk.getValue().owner.toString().equals(uuid)) {
						for(int x = 0; x < 16; x++) {
							for(int z = 0; z < 16; z++) {
								World world = player.getWorld();
								player.spawnParticle(Particle.VILLAGER_HAPPY,
										new Location(world, a + x + 0.5f,
												world.getHighestBlockAt(a + x, b + z).getY() + 1, b + z + 0.5f),
										1, 0, 0, 0);
							}
						}
					}
				}
			} else if(args[0].equalsIgnoreCase("invite")) {
				if(args.length != 2) {
					sender.sendMessage(ChatColor.RED + "Incorrect arguments: /kingdom invite <player_name>");
					return true;
				}
				King otherKing = null;
				try {
					otherKing = getAnotherKing(args[1]);
				} catch(NullPointerException ignored) {
				}
				if(otherKing != null) {
					if(otherKing.equals(thisKing)) {
						sender.sendMessage("You can't invite yourself to your kingdom!");
						return true;
					}
					sender.sendMessage("King has his own lands to rule!");
					return true;
				}
				Player invitedPlayer;
				try {
					invitedPlayer = Objects.requireNonNull(Bukkit.getPlayer(args[1]));
				} catch(NullPointerException e) {
					sender.sendMessage(ChatColor.RED + "No such player found.");
					return true;
				}
				Data.getInstance().kingdomRequests.put(invitedPlayer.getUniqueId(), thisKing);
				invitedPlayer.sendMessage("An invite to join kingdom as an advisor\nfrom " + thisKing.getFullTitle() +
						"\nUse §a/accept join§f to accept invitation.");
				sender.sendMessage("Invite sent.");
			} else {
				sendCommandList(sender);
			}
			return true;
		}
		return false;
	}

	public class PluginTabCompleter implements TabCompleter {
		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, Command cde, String arg, String[] args) {
			if(args.length < 2) {
				return Arrays.asList("setname", "info", "show", "invite");
			} else if(args[0].equalsIgnoreCase("invite")) {
				return Bukkit.getServer().getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
			} else return Collections.emptyList();
		}
	}

	public King getAnotherKing(String name) {
		UUID otherUuid;
		otherUuid = Objects.requireNonNull(Bukkit.getPlayer(name)).getUniqueId();
		if(!kingData.get(otherUuid.toString()).isPresent()) {
			return null;
		}
		return kingData.get(otherUuid.toString()).get();
	}
}
