package tld.sofugames.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DiploCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("diplomacy")) {
			if(args != null && args.length != 0) {

			}
			else {
				sender.sendMessage(ChatColor.GOLD + "Diplomacy command:");
				sender.sendMessage(ChatColor.GOLD + "info" + ChatColor.WHITE + ": gives information about your kingdom " +
						"relations with another kingdom.");
				sender.sendMessage(ChatColor.GOLD + "aid" + ChatColor.WHITE + ": gives a possibility to give a " +
						"foreign aid to support allies with money.");
				sender.sendMessage(ChatColor.GOLD + "ally" + ChatColor.WHITE + ": sends alliance request to another kingdom.");
				sender.sendMessage(ChatColor.GOLD + "war" + ChatColor.WHITE + ": gives a possibility to declare a war on another kingdom.");
			}
			return true;
		}
		return false;
	}

	public class PluginTabCompleter implements TabCompleter {
		@Override
		public List<String> onTabComplete(CommandSender sender, Command cde, String arg, String[] args) {
			if(args.length < 2) {
				return Arrays.asList("info", "aid", "ally", "war");
			}
			return Collections.emptyList();
		}
	}
}
