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
import tld.sofugames.data.*;
import tld.sofugames.gui.WarGui;
import tld.sofugames.models.ClaimedChunk;
import tld.sofugames.models.King;
import tld.sofugames.models.War;
import tld.sofugames.rot.ChunkType;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class WarCommand implements CommandExecutor {
	public HashMap<String, King> requests = new HashMap<>();
	DaoFactory daoFactory = new DaoFactory();
	KingDao kingData = daoFactory.getKings();
	ClaimDao claimData = daoFactory.getClaims();
	WarDao wars = daoFactory.getWars();

	//.orElse(new King()) is used because command can be used only with rotr.king permission
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("war")) {
			if(args != null && args.length != 0) {
				if(args[0].equalsIgnoreCase("declare")) {
					if(args.length != 2) {
						sender.sendMessage(ChatColor.RED + "Incorrect arguments: /war declare <king_name>");
						return true;
					}
					if(!kingData.get(Bukkit.getPlayer(args[1]).getUniqueId().toString()).isPresent()) {
						sender.sendMessage(ChatColor.RED + "No king with this username found.");
						return true;
					}
					King thisKing = kingData.get(((Player) sender).getUniqueId().toString()).orElse(new King());
					King otherKing = kingData.get(Bukkit.getPlayer(args[1]).getUniqueId().toString()).get();
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
					wars.save(war);
					WarGui warGui = new WarGui();
					((Player) sender).openInventory(warGui.getInventory());
				} else if(args[0].equalsIgnoreCase("info")) {
					King king = kingData.get(((Player) sender).getUniqueId().toString()).orElse(new King());
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
					King king = kingData.get(((Player) sender).getUniqueId().toString()).orElse(new King());
					if(!king.isAtWar()) {
						sender.sendMessage("§cYou must be at war to end it!");
						return true;
					}
					if(king.isWarAlly()) {
						sender.sendMessage("§cAllies can't sign peace.");
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
							requests.put(king.getUuid().toString(), enemy);
							BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
							scheduler.scheduleSyncDelayedTask(Data.getInstance().plugin, () -> {
								if(requests.containsKey(king.getUuid().toString())) {
									requests.remove(king.getUuid().toString());
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
					King king = kingData.get(((Player) sender).getUniqueId().toString()).orElse(new King());
					if(!king.isAtWar()) {
						sender.sendMessage("§cYou must be at war to end it!");
						return true;
					}
					if(!requests.containsKey(king.getUuid().toString())) {
						sender.sendMessage("You have no pending requests.");
						return true;
					}
					requests.remove(king.getUuid().toString());
					King enemy = king.getCurrentWar().getDef().equals(king) ?
							king.getCurrentWar().getAtk() : king.getCurrentWar().getDef();

					king.getCurrentWar().signWhitePeace(true);
					sendPeaceMessage(king, enemy, true);
				} else if(args[0].equalsIgnoreCase("claim")) {
					Player player = (Player) sender;
					King king = kingData.get(player.getUniqueId().toString()).orElse(new King());
					if(king.isAtWar()) {
						sender.sendMessage(ChatColor.RED + "You cannot unclaim war claims during wars!");
						return true;
					}
					Chunk targetChunk = player.getLocation().getChunk();
					if(isNeighboring(targetChunk, king)) {
						//isNeighboring already checks if chunk is null
						ClaimedChunk chunk = claimData.get(targetChunk.toString()).orElse(new ClaimedChunk());
						try {
							king.addWarClaim(chunk);
						} catch(SQLException e) {
							e.printStackTrace();
							return true;
						}
						King otherKing = kingData.get(chunk.owner.toString()).orElse(new King());
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
					King king = kingData.get(player.getUniqueId().toString()).orElse(new King());
					if(king.isAtWar()) {
						sender.sendMessage(ChatColor.RED + "You cannot change war claims during wars!");
						return true;
					}
					Chunk targetChunk = player.getLocation().getChunk();
					if(claimData.get(targetChunk.toString()).isPresent()) {
						ClaimedChunk chunk = claimData.get(targetChunk.toString()).get();
						if(king.warClaims.contains(chunk)) {
							try {
								king.deleteWarClaim(chunk);
							} catch(SQLException e) {
								e.printStackTrace();
								return true;
							}
							King otherKing = kingData.get(chunk.owner.toString()).orElse(new King());
							king.changeMeaning(otherKing.getUuid(), 10);
							otherKing.changeMeaning(king.getUuid(), 10);
							otherKing.assignedPlayer
									.sendMessage("King " + king.fullTitle + " revokes some of their claims.");
							king.assignedPlayer.sendMessage("War claim revoked.");
						} else {
							king.assignedPlayer.sendMessage("You don't have any war claim on this land.");
						}
					}
				} else if(args[0].equalsIgnoreCase("join")) {
					if(args.length != 2) {
						sender.sendMessage(ChatColor.RED + "Incorrect arguments: /war join <player_of_side>");
						return true;
					}
					if(!Data.isKing(args[1])) {
						sender.sendMessage(ChatColor.RED + "Player is not a king.");
						return true;
					}
					King thisKing = kingData.get(((Player) sender).getUniqueId().toString()).orElse(new King());
					if(thisKing.isAtWar()) {
						sender.sendMessage(ChatColor.RED + "You are already at war!");
						return true;
					}
					King otherKing = kingData.get(Bukkit.getPlayer(args[1]).getUniqueId().toString()).orElse(new King());
					if(thisKing.equals(otherKing)) {
						sender.sendMessage(ChatColor.RED + "Can't declare war on yourself!");
						return true;
					}
					if(!thisKing.allies.contains(otherKing)) {
						sender.sendMessage(ChatColor.RED + "You are not allied with this king!");
						return true;
					}
					if(!otherKing.isAtWar()) {
						sender.sendMessage(ChatColor.RED + "Your ally is not at war!");
						return true;
					}
					otherKing.getCurrentWar().addAlly(otherKing, thisKing);
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
				return Arrays.asList("claim", "declare", "info", "end", "whitepeace", "acceptpeace", "join");
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
		if(claimData.get(targetChunk.toString()).isPresent()) {
			ClaimedChunk ch = claimData.get(targetChunk.toString()).get();
			if(king.warClaims.contains(ch)) {
				return false;
			}
			for(Chunk chunk : ch.getRelatives()) {
				if(claimData.get(chunk.toString()).isPresent()) {
					ClaimedChunk neighbor = claimData.get(chunk.toString()).get();
					//if chunk exists owner exists
					if((neighbor.owner.equals(king.getUuid()) || king.warClaims.contains(neighbor)) &&
					!kingData.get(neighbor.owner.toString()).orElse(new King()).homeChunk.equals(neighbor)) {
						return true;
					}
				}
			}
		}
		return false;
	}
}


