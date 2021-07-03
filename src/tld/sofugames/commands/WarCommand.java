package tld.sofugames.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
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
import tld.sofugames.models.ClaimedChunk;
import tld.sofugames.models.King;
import tld.sofugames.models.War;
import tld.sofugames.rot.ChunkType;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WarCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("war")) {
			if(args != null && args.length != 0) {
				if(args[0].equalsIgnoreCase("declare")) {
					if(args.length != 2) {
						sender.sendMessage(ChatColor.RED + "Incorrect arguments: /war declare <king_name>");
						return true;
					}
					if(!Data.isKing(args[1])) {
						sender.sendMessage(ChatColor.RED + "No king with this username found.");
						return true;
					}
					King thisKing = Data.getInstance().kingData.get(((Player) sender).getUniqueId().toString());
					King otherKing = Data.getInstance().kingData.get(Bukkit.getPlayer(args[1]).getUniqueId().toString());
					if(thisKing.equals(otherKing)) {
						sender.sendMessage(ChatColor.RED + "Can't declare war on yourself!");
						return true;
					}
					if(!thisKing.relations.containsKey(otherKing.getUuid())) {
						sender.sendMessage(ChatColor.RED + "You haven't contacted with this king yet and have no relations with them.");
						return true;
					}
					if(thisKing.isAtWar()) {
						sender.sendMessage(ChatColor.RED + "You are already at war!");
						return true;
					}
					War war = new War(thisKing, otherKing);
					Data.getInstance().wars.put(((Player) sender).getUniqueId().toString(), war);
					WarGui warGui = new WarGui();
					((Player) sender).openInventory(warGui.getInventory());
				} else if(args[0].equalsIgnoreCase("info")) {
					King king = Data.getInstance().kingData.get(((Player) sender).getUniqueId().toString());
					if(king.isAtWar()) {
						War war = king.getCurrentWar();
						float score = (war.getDef().equals(king) ? -1 : 1) * war.getScore() * 100;
						sender.sendMessage("§e" + war.getWarType().getName() + " war.\n§6Attacker:§f " + war.getAtk().fullTitle +
								"\n§6Defender:§f " + war.getDef().fullTitle + "\n§fScore: " + (score < 0 ? "§c" : "§a") +
								String.format("%.2f", score) + "%");
					} else {
						sender.sendMessage("You have peace on your grounds... for now.");
					}
				} else if(args[0].equalsIgnoreCase("end") || args[0].equalsIgnoreCase("whitepeace")) {
					King king = Data.getInstance().kingData.get(((Player) sender).getUniqueId().toString());
					if(!king.isAtWar()) {
						sender.sendMessage("§cYou must be at war to end it!");
						return true;
					}
					King enemy = king.getCurrentWar().getDef().equals(king) ?
							king.getCurrentWar().getAtk() : king.getCurrentWar().getDef();
					boolean canBeFinished;
					King atk = king.getCurrentWar().getAtk();
					King def = king.getCurrentWar().getDef();
					if(args[0].equalsIgnoreCase("end")) {
						canBeFinished = king.getCurrentWar().signPeace();
					} else {
						canBeFinished = king.getCurrentWar().signWhitePeace(false);
						if(!canBeFinished) {
							enemy.assignedPlayer.sendMessage("§6Enemy offers to sign a white peace.\n" +
									"§fUse §a/war acceptpeace §fto answer, otherwise ignore this message.\n" +
									"You hase 3 minutes to choose.");
							Data.getInstance().requests.put(king.getUuid().toString(), enemy);
							BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
							scheduler.scheduleSyncDelayedTask(Data.getInstance().plugin, () -> {
								if(Data.getInstance().requests.containsKey(king.getUuid().toString())) {
									Data.getInstance().requests.remove(king.getUuid().toString());
									king.assignedPlayer.sendMessage("§cPeace offer was rejected.");
								}
							}, 3600L);
							return true;
						}
					}
					if(!canBeFinished) {
						sender.sendMessage("§cRequirements aren't met!");
						return true;
					} else {
						sendPeaceMessage(atk, def, false);
					}
				} else if(args[0].equalsIgnoreCase("acceptpeace")) {
					King king = Data.getInstance().kingData.get(((Player) sender).getUniqueId().toString());
					if(!king.isAtWar()) {
						sender.sendMessage("§cYou must be at war to end it!");
						return true;
					}
					Data.getInstance().requests.remove(king.getUuid().toString());
					King enemy = king.getCurrentWar().getDef().equals(king) ?
							king.getCurrentWar().getAtk() : king.getCurrentWar().getDef();

					king.getCurrentWar().signWhitePeace(true);
					sendPeaceMessage(king, enemy, true);
				} else if(args[0].equalsIgnoreCase("claim")) {
					Player player = (Player) sender;
					King king = Data.getInstance().kingData.get(player.getUniqueId().toString());
					Chunk targetChunk = player.getLocation().getChunk();
					if(isNeighboring(targetChunk, king)) {
						ClaimedChunk chunk = Data.getInstance().claimData.get(targetChunk.toString());
						try {
							king.addWarClaim(chunk);
						} catch(SQLException e) {
							e.printStackTrace();
							return true;
						}
						King otherKing = Data.getInstance().kingData.get(chunk.owner.toString());
						king.changeMeaning(otherKing.getUuid(), -50);
						otherKing.changeMeaning(king.getUuid(), -80);
						otherKing.assignedPlayer
								.sendMessage("§cKing " + king.fullTitle + " acquired a war claim on one of our chunks! ");
						king.assignedPlayer.sendMessage("War claim acquired.");
					} else {
						king.assignedPlayer.sendMessage("§cTarget chunk is not owned by any king or it's not " +
								"neighboring to our borders or other war claims.");
					}
				} else if(args[0].equalsIgnoreCase("unclaim")) {
					Player player = (Player) sender;
					King king = Data.getInstance().kingData.get(player.getUniqueId().toString());
					Chunk targetChunk = player.getLocation().getChunk();
					if(Data.getInstance().claimData.containsKey(targetChunk.toString())) {
						ClaimedChunk chunk = Data.getInstance().claimData.get(targetChunk.toString());
						if(king.warClaims.contains(chunk)) {
							try {
								king.deleteWarClaim(chunk);
							} catch(SQLException e) {
								e.printStackTrace();
								return true;
							}
							King otherKing = Data.getInstance().kingData.get(chunk.owner.toString());
							king.changeMeaning(otherKing.getUuid(), 10);
							otherKing.changeMeaning(king.getUuid(), 10);
							otherKing.assignedPlayer
									.sendMessage("King " + king.fullTitle + " revokes some of their claims.");
							king.assignedPlayer.sendMessage("War claim revoked.");
						} else {
							king.assignedPlayer.sendMessage("You don't have any war claim on this land.");
						}
					}
				}
			} else {
				sender.sendMessage(ChatColor.GOLD + "War command commands:");
				sender.sendMessage(ChatColor.GOLD + "claim" + ChatColor.WHITE + ": lets you claim an enemy chunk for expansion.");
				sender.sendMessage(ChatColor.GOLD + "unclaim" + ChatColor.WHITE + ": lets you unclaim an enemy chunk.");
				sender.sendMessage(ChatColor.GOLD + "declare" + ChatColor.WHITE + ": gives a possibility to declare a war on another kingdom.");
				sender.sendMessage(ChatColor.GOLD + "info" + ChatColor.WHITE + ": shows info about your current war, if there's any.");
				sender.sendMessage(ChatColor.GOLD + "end" + ChatColor.WHITE + ": lets you end the war when you won.");
				sender.sendMessage(ChatColor.GOLD + "whitepeace" + ChatColor.WHITE + ": lets you sign the white peace.");
				sender.sendMessage(ChatColor.GOLD + "acceptpeace" + ChatColor.WHITE + ": you can confirm a white peace.");
			}
			return true;
		}
		return false;
	}

	public class PluginTabCompleter implements TabCompleter {
		@Override
		public List<String> onTabComplete(CommandSender sender, Command cde, String arg, String[] args) {
			if(args.length < 2) {
				return Arrays.asList("claim", "declare", "info", "end", "whitepeace", "acceptpeace");
			} else if(args.length == 2) {
				return Bukkit.getServer().getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
			} else return Collections.emptyList();
		}

	}

	private void sendPeaceMessage(King atk, King def, boolean isWhitePeace) {
		if(isWhitePeace) {
			atk.assignedPlayer.sendTitle(ChatColor.GREEN + "White peace!", "", 20, 70, 20);
			atk.assignedPlayer.playSound(atk.assignedPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
			def.assignedPlayer.sendTitle(ChatColor.GREEN + "White peace!", "", 20, 70, 20);
			def.assignedPlayer.playSound(def.assignedPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
		} else {
			atk.assignedPlayer.sendTitle(ChatColor.GREEN + "Peace!", "", 20, 70, 20);
			atk.assignedPlayer.playSound(atk.assignedPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
			def.assignedPlayer.sendTitle(ChatColor.RED + "War lost.", "", 20, 70, 20);
			def.assignedPlayer.playSound(def.assignedPlayer.getLocation(), Sound.ENTITY_ENDERMAN_DEATH, 1, 1);
		}
	}

	private boolean isNeighboring(Chunk targetChunk, King king) {
		if(Data.getInstance().claimData.containsKey(targetChunk.toString())) {
			ClaimedChunk ch = Data.getInstance().claimData.get(targetChunk.toString());
			if(king.warClaims.contains(ch)) {
				return false;
			}
			for(Chunk chunk : ch.getRelatives()) {
				if(Data.getInstance().claimData.containsKey(chunk.toString())) {
					ClaimedChunk neighbor = Data.getInstance().claimData.get(chunk.toString());
					if((neighbor.owner.equals(king.getUuid()) || king.warClaims.contains(neighbor)) &&
					!Data.getInstance().kingData.get(neighbor.owner.toString()).homeChunk.equals(neighbor)) {
						return true;
					}
				}
			}
		}
		return false;
	}
}


