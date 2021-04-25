package tld.sofugames.listeners;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import tld.sofugames.data.*;
import tld.sofugames.models.*;

import java.sql.SQLException;
import java.util.*;

public class EventListener implements Listener {

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBreak(BlockBreakEvent event) {
		if(isWorld(event.getPlayer())) {
			if(!checkOwnership(event.getPlayer(), event.getBlock())) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED + "This land is not owned by you or your kingdom!");
			} else {
				if(Tag.BEDS.getValues().contains(event.getBlock().getType())) {
					if(Data.getInstance().houseData.containsKey(Data.getInstance().getBedHash(event.getBlock()))) {
						try {
							event.getPlayer().sendMessage("Bed was destroyed");
							Data.getInstance().houseData.get(Data.getInstance().getBedHash(event.getBlock())).delete(Data.getInstance().getConnection());
							event.setDropItems(false);
						} catch(SQLException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if(isWorld(player)) {
			if(!checkOwnership(event.getPlayer(), event.getBlock())) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED + "This land is not owned by you or your kingdom!");
			}
		}
	}
	
	public static boolean isWorld(Player player) {
		World.Environment env = player.getWorld().getEnvironment();
		return env != World.Environment.NETHER && env != World.Environment.THE_END;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(Data.getInstance().kingData.containsKey(player.getUniqueId().toString())) {
			King king = Data.getInstance().kingData.get(player.getUniqueId().toString());
			king.assignedPlayer = player;
			king.setBossBar();
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRespawn(PlayerRespawnEvent event) {
		Player deceasedPlayer = event.getPlayer();
		if(Data.getInstance().kingData.containsKey(deceasedPlayer.getUniqueId().toString())) {
			BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
			deceasedPlayer.setGameMode(GameMode.SPECTATOR);
			King king = Data.getInstance().kingData.get(deceasedPlayer.getUniqueId().toString());
			king.changeGen();
			event.setRespawnLocation(king.homeChunk.world.getBlock(7, 150, 7).getLocation());
			scheduler.scheduleSyncDelayedTask(Objects.requireNonNull(Bukkit.getPluginManager().getPlugins()[0]),
					() -> rebirth(king), 6000L);
			deceasedPlayer.sendMessage(ChatColor.RED + "Waiting for your resurrection...");
			int randomSong = new Random().nextInt(101);
			deceasedPlayer.stopSound(Sound.MUSIC_CREATIVE);
			deceasedPlayer.stopSound(Sound.MUSIC_GAME);
			Location musicLoc = king.homeChunk.world.getBlock(7, 150, 7).getLocation();
			if(randomSong > 90) {
				deceasedPlayer.sendMessage(rainbowText("hardbassss"));
				deceasedPlayer.playSound(musicLoc, Sound.MUSIC_DISC_PIGSTEP, 0.7f, 1);
			} else if(randomSong < 5) {
				deceasedPlayer.sendMessage(ChatColor.ITALIC + "" + ChatColor.AQUA + "lmao get stal'd");
				deceasedPlayer.playSound(musicLoc, Sound.MUSIC_DISC_STAL, 0.7f, 1);
			} else {
				deceasedPlayer.playSound(musicLoc, Sound.MUSIC_DISC_STRAD, 0.7f, 1);
			}
		}
	}

	public String rainbowText(String text) {
		ChatColor[] rainbow = new ChatColor[] {ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW, ChatColor.GREEN,
		ChatColor.AQUA, ChatColor.BLUE, ChatColor.DARK_PURPLE};
		int rainbowIndex = 0;
		StringBuilder rainbowText = new StringBuilder();
		for(String i: text.split("")) {
			rainbowText.append(rainbow[rainbowIndex]).append(i);
			rainbowIndex++;
			if(rainbowIndex == rainbow.length) {
				rainbowIndex = 0;
			}
		}
		return rainbowText.toString();
	}

	public void rebirth(King deceasedKing) {
		Player player = deceasedKing.assignedPlayer;
		Location highestAtCenter = player.getWorld().getHighestBlockAt(
				deceasedKing.homeChunk.world.getBlock(7, 0, 7).getLocation()).getLocation();
		highestAtCenter.add(0, 1, 0);
		player.teleport(highestAtCenter);
		player.setGameMode(GameMode.SURVIVAL);
		Data.getInstance().giveBed(player, false);
		player.sendTitle(ChatColor.GOLD + "Glory to the new King!", deceasedKing.fullTitle, 20, 70, 20);
		player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
	}

	public static boolean checkOwnership(Player player, Block block) {
		UUID senderUUID = player.getUniqueId();
		ClaimedChunk chunk = Data.getInstance().claimData.get(block.getChunk().toString());
		if(block.getY() > 40) {
			if(chunk != null) {
				if(!chunk.owner.equals(senderUUID)) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}
}