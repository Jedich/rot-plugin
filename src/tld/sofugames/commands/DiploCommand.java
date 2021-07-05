package tld.sofugames.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import tld.sofugames.data.Data;
import tld.sofugames.gui.WarGui;
import tld.sofugames.listeners.WarGuiListener;
import tld.sofugames.models.King;
import tld.sofugames.models.War;

import java.util.*;
import java.util.stream.Collectors;

public class DiploCommand implements CommandExecutor {
	public HashMap<UUID, King> requests = new HashMap<>();
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("diplomacy")) {
			if(args != null && args.length != 0) {
				if(args[0].equalsIgnoreCase("info")) {
					if(args.length == 2) {
						UUID otherUuid;
						try {
							otherUuid = Bukkit.getPlayer(args[1]).getUniqueId();
						} catch(NullPointerException e) {
							sender.sendMessage(ChatColor.RED + "No such player found.");
							return true;
						}
						King thisKing = Data.getInstance().kingData.get(((Player) sender).getUniqueId().toString());
						King otherKing =  Data.getInstance().kingData.get(otherUuid.toString());
						if(thisKing.equals(otherKing)) {
							sender.sendMessage(ChatColor.RED + "You can't aid yourself.");
							return true;
						}
						sender.sendMessage("Relations with " + ChatColor.GOLD + otherKing.assignedPlayer.getDisplayName() + ":\n"
								+ "Your opinion: " + ChatColor.WHITE + thisKing.relations.get(otherKing.getUuid()) + "\n" +
								ChatColor.GOLD + "Their opinion: " + ChatColor.WHITE + otherKing.relations.get(thisKing.getUuid()));
					} else {
						sender.sendMessage(ChatColor.RED + "Incorrect arguments: /diplomacy info <king_name>");
					}
				}
				else if(args[0].equalsIgnoreCase("aid")) {
					if(args.length == 3) {
						float cost = 0;
						try {
							cost = Float.parseFloat(args[2]);
						} catch(NumberFormatException e) {
							sender.sendMessage(ChatColor.RED + "Please specify a correct number value!");
							return true;
						}
						UUID otherUuid;
						try {
							otherUuid = Bukkit.getPlayer(args[1]).getUniqueId();
						} catch(NullPointerException e) {
							sender.sendMessage(ChatColor.RED + "No such player found.");
							return true;
						}
						King thisKing = Data.getInstance().kingData.get(((Player) sender).getUniqueId().toString());
						King otherKing =  Data.getInstance().kingData.get(otherUuid.toString());
						if(thisKing.equals(otherKing)) {
							sender.sendMessage(ChatColor.RED + "You can't aid yourself.");
							return true;
						}
						if(thisKing.getGoldBalance() < cost) {
							sender.sendMessage(ChatColor.RED + "Not enough money to perform this operation.");
							return true;
						}
						thisKing.contact(otherKing);
						otherKing.setGoldBalance(cost);
						thisKing.setGoldBalance(-cost);
						otherKing.assignedPlayer.sendMessage(thisKing.assignedPlayer.getDisplayName() + ChatColor.WHITE
								+ " sent a gift: " + ChatColor.GREEN + "" + cost + "g.");
						thisKing.assignedPlayer.sendMessage("Aid sent successfully.");
						thisKing.assignedPlayer.playSound(thisKing.assignedPlayer.getLocation(),
								Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
						otherKing.assignedPlayer.playSound(otherKing.assignedPlayer.getLocation(),
								Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
						otherKing.changeMeaning(thisKing.getUuid(), 15);
					} else {
						sender.sendMessage(ChatColor.RED + "Incorrect arguments: /diplomacy aid <king_name> <value>");
					}
				} else if(args[0].equalsIgnoreCase("ally")) {
					if(args.length != 2) {
						sender.sendMessage(ChatColor.RED + "Incorrect arguments: /diplomacy ally <player_name>");
						return true;
					}
					if(!Data.isKing(args[1])) {
						sender.sendMessage(ChatColor.RED + "Player is not a king.");
						return true;
					}
					King thisKing = Data.getInstance().kingData.get(((Player) sender).getUniqueId().toString());
					Player invitedPlayer = Bukkit.getPlayer(args[1]);
					invitedPlayer.sendMessage(thisKing.fullTitle + "§6 offers to form an alliance.\n" +
							"§fUse §a/diplomacy allyaccept §fto answer, otherwise ignore this message.\n" +
							"You hase 3 minutes to answer.");
					BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
					scheduler.scheduleSyncDelayedTask(Data.getInstance().plugin, () -> {
						if(requests.containsKey(invitedPlayer.getUniqueId())) {
							requests.remove(invitedPlayer.getUniqueId());
							thisKing.assignedPlayer.sendMessage("§cPeace offer was rejected.");
						}
					}, 3600L);
				} else if(args[0].equalsIgnoreCase("allyaccept")) {
					King king = Data.getInstance().kingData.get(((Player) sender).getUniqueId().toString());
					if(!requests.containsKey(king.getUuid())) {
						sender.sendMessage("You have no pending requests.");
						return true;
					}
					King otherKing = requests.get(king.getUuid());
					requests.remove(king.getUuid());
					try {
						king.addAlly(otherKing);
					} catch(IllegalArgumentException e) {
						sender.sendMessage("Cannot accept alliance: " + e.getMessage());
						otherKing.assignedPlayer.sendMessage("Cannot accept alliance: " + e.getMessage());
					}
					sender.sendMessage("§aAlliance formed!");
					otherKing.assignedPlayer.sendMessage("§aAlliance formed!");
				} else if(args[0].equalsIgnoreCase("allyrevoke")) {
					if(args.length != 2) {
						sender.sendMessage(ChatColor.RED + "Incorrect arguments: /diplomacy allyrevoke <player_name>");
						return true;
					}
					if(!Data.isKing(args[1])) {
						sender.sendMessage(ChatColor.RED + "Player is not a king.");
						return true;
					}
					King thisKing = Data.getInstance().kingData.get(((Player) sender).getUniqueId().toString());
					King otherKing = Data.getInstance().kingData.get(Bukkit.getPlayer(args[1]).getUniqueId().toString());
					if(!thisKing.allies.contains(otherKing)) {
						sender.sendMessage(ChatColor.RED + "This king is not your ally.");
						return true;
					}
					try {
						thisKing.deleteAlly(otherKing);
					} catch(IllegalArgumentException e) {
						sender.sendMessage("Cannot revoke alliance: " + e.getMessage());
					}
					sender.sendMessage("Alliance revoked.");
					otherKing.assignedPlayer.sendMessage("§сAlliance with " +
							thisKing.fullTitle + "§с was revoked by other side!");
				}
			} else {
				sender.sendMessage(ChatColor.GOLD + "Diplomacy command:");
				sender.sendMessage(ChatColor.GOLD + "info" + ChatColor.WHITE + ": gives information about your kingdom " +
						"relations with another kingdom.");
				sender.sendMessage(ChatColor.GOLD + "aid" + ChatColor.WHITE + ": gives a possibility to give a " +
						"foreign aid to support allies with money.");
				sender.sendMessage(ChatColor.GOLD + "ally" + ChatColor.WHITE + ": sends alliance request to another kingdom.");
				sender.sendMessage(ChatColor.GOLD + "allyaccept" + ChatColor.WHITE + ": accepts alliance request.");
				sender.sendMessage(ChatColor.GOLD + "allyrevoke" + ChatColor.WHITE + ": revokes existing alliance.");
			}
			return true;
		}
		return false;
	}

	public class PluginTabCompleter implements TabCompleter {
		@Override
		public List<String> onTabComplete(CommandSender sender, Command cde, String arg, String[] args) {
			if(args.length < 2) {
				return Arrays.asList("info", "aid", "ally", "allyaccept", "allyrevoke");
			} else if(arg.equalsIgnoreCase("allyaccept") || arg.equalsIgnoreCase("allyrevoke")) {
				return Collections.emptyList();
			} else if(args.length < 3) {
				return Bukkit.getServer().getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
			} else return Collections.emptyList();
		}
	}
}
