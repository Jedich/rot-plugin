package tld.sofugames.listeners;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitScheduler;
import tld.sofugames.dao.impl.ClaimDao;
import tld.sofugames.dao.impl.DaoFactory;
import tld.sofugames.dao.impl.HouseDao;
import tld.sofugames.dao.impl.KingDao;
import tld.sofugames.data.*;
import tld.sofugames.models.*;

import java.util.*;

public class EventListener implements Listener {

	DaoFactory daoFactory = new DaoFactory();
	KingDao kingData = daoFactory.getKings();
	HouseDao houseData = daoFactory.getHouses();

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBreak(BlockBreakEvent event) {
		if(isWorld(event.getPlayer())) {
			if(!canInteract(event.getPlayer(), event.getBlock())) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED + "This land is not owned by you or your kingdom!");
			} else {
				if(Tag.BEDS.getValues().contains(event.getBlock().getType())) {
					if(houseData.get(Data.getInstance().getBedHash(event.getBlock())).isPresent()) {
						event.getPlayer().sendMessage("Bed was destroyed");
						houseData.delete(houseData.get(Data.getInstance().getBedHash(event.getBlock())).get());
						event.setDropItems(false);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		checkInteraction(event.getPlayer(), event.getBlock(), event);
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(e.getClickedBlock() != null) {
			checkInteraction(e.getPlayer(), e.getClickedBlock(), e);
		}
	}

	public void checkInteraction(Player player, Block block, Cancellable event) {
		if(isWorld(player)) {
			if(!canInteract(player, block)) {
				player.sendMessage(ChatColor.RED + "This land is not owned by you or your kingdom!");
				event.setCancelled(true);
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
		if(kingData.get(player.getUniqueId().toString()).isPresent()) {
			King king = kingData.get(player.getUniqueId().toString()).get();
			king.assignedPlayer = player;
			king.setBossBar();
			king.setUuid(player.getUniqueId());
		}
	}


	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRespawn(PlayerRespawnEvent event) {
		Player deceasedPlayer = event.getPlayer();
		if(kingData.get(deceasedPlayer.getUniqueId().toString()).isPresent()) {
			King king = kingData.get(deceasedPlayer.getUniqueId().toString()).get();
			if(king.isAtWar()) {
				return;
			}
			BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
			deceasedPlayer.setGameMode(GameMode.SPECTATOR);
			king.changeGen();
			event.setRespawnLocation(king.homeChunk.world.getBlock(7, 150, 7).getLocation());
			scheduler.scheduleSyncDelayedTask(Data.getInstance().plugin, () -> rebirth(king), 4800L);
			Data.getInstance().timers.put(deceasedPlayer.getUniqueId(), System.currentTimeMillis() + (4 * 60 * 1000));
			deceasedPlayer.sendMessage(ChatColor.AQUA + "Waiting for your resurrection...");
			deceasedPlayer.sendMessage(ChatColor.AQUA + "Use /timeleft to check how much time you need to wait!");
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
		ChatColor[] rainbow = new ChatColor[]{ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW, ChatColor.GREEN,
				ChatColor.AQUA, ChatColor.BLUE, ChatColor.DARK_PURPLE};
		int rainbowIndex = 0;
		StringBuilder rainbowText = new StringBuilder();
		for(String i : text.split("")) {
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
		player.sendTitle(ChatColor.GOLD + "Glory to the New King!", deceasedKing.getFullTitle(), 20, 70, 20);
		player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
	}

	public static boolean canInteract(Player player, Block block) {
		UUID senderUUID = player.getUniqueId();
		ClaimDao claimData = new DaoFactory().getClaims();
		if(block.getY() <= 16 || block.getY() >= 40) {
			if(claimData.get(block.getChunk().toString()).isPresent()) {
				ClaimedChunk chunk = claimData.get(block.getChunk().toString()).get();
				//claimed chunk without owner is impossible
				King owner = new DaoFactory().getKings().get(chunk.owner.toString()).orElse(new King());
				return chunk.owner.equals(senderUUID) || owner.advisors.contains(senderUUID);
			}
		}
		return true;
	}
}