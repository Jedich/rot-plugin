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
import tld.sofugames.data.Data;
import tld.sofugames.gui.WarGui;
import tld.sofugames.listeners.WarGuiListener;
import tld.sofugames.models.King;
import tld.sofugames.models.War;

import java.util.*;
import java.util.stream.Collectors;

public class DiploCommand implements CommandExecutor {

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
				}
				else if(args[0].equalsIgnoreCase("war")) {
					if(args.length != 2) {
						sender.sendMessage(ChatColor.RED + "Incorrect arguments: /diplomacy war <king_name>");
						return true;
					}
					if(!Data.isKing(args[1])) {
						sender.sendMessage(ChatColor.RED + "No king with this username found.");
						return true;
					}
					King thisKing = Data.getInstance().kingData.get(((Player)sender).getUniqueId().toString());
					King otherKing = Data.getInstance().kingData.get(Bukkit.getPlayer(args[1]).getUniqueId().toString());
					if(thisKing.isAtWar()) {
						sender.sendMessage(ChatColor.RED + "You are already at war!");
						return true;
					}
					War war = new War(thisKing, otherKing);
					Data.getInstance().wars.put(((Player)sender).getUniqueId().toString(), war);
					WarGui warGui = new WarGui();
					((Player)sender).openInventory(warGui.getInventory());
				}
				else if(args[0].equalsIgnoreCase("currentwar")) {
					King king = Data.getInstance().kingData.get(((Player)sender).getUniqueId().toString());
					if(king.isAtWar()) {
						War war = king.getCurrentWar();
						sender.sendMessage("§e" + war.getWarType().getName() + " war.\n§6Attacker:§f " + war.getAtk().fullTitle +
								"\n§6Defender:§f " + war.getDef().fullTitle + "\n§fScore: " + war.getScore()*100 + "%");
					} else {
						sender.sendMessage("You have peace on your grounds... for now.");
					}
				}
			} else {
				sender.sendMessage(ChatColor.GOLD + "Diplomacy command:");
				sender.sendMessage(ChatColor.GOLD + "info" + ChatColor.WHITE + ": gives information about your kingdom " +
						"relations with another kingdom.");
				sender.sendMessage(ChatColor.GOLD + "aid" + ChatColor.WHITE + ": gives a possibility to give a " +
						"foreign aid to support allies with money.");
				sender.sendMessage(ChatColor.GOLD + "ally" + ChatColor.WHITE + ": sends alliance request to another kingdom.");
				sender.sendMessage(ChatColor.GOLD + "war" + ChatColor.WHITE + ": gives a possibility to declare a war on another kingdom.");
				sender.sendMessage(ChatColor.GOLD + "currentwar" + ChatColor.WHITE + ": shows info about your current war, if there's any.");
			}
			return true;
		}
		return false;
	}

	public class PluginTabCompleter implements TabCompleter {
		@Override
		public List<String> onTabComplete(CommandSender sender, Command cde, String arg, String[] args) {
			if(args.length < 2) {
				return Arrays.asList("info", "aid", "ally", "war", "currentwar");
			}
			return Bukkit.getServer().getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
		}
	}
}
