package com.jedich.listeners;

import com.jedich.dao.impl.*;
import com.jedich.data.Data;
import com.jedich.models.ClaimedChunk;
import com.jedich.models.DeferredEvent;
import com.jedich.models.King;
import com.jedich.rot.DeferredEventType;
import org.apache.commons.lang3.time.DateUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;

public class EventListener implements Listener {

	DaoFactory daoFactory = new DaoFactory();
	KingDao kingData = daoFactory.getKings();
	EventDao deferredEventData = daoFactory.getDeferredEvents();
	HouseDao houseData = daoFactory.getHouses();

	private static final long DEATH_COOLDOWN_TICKS = 4800L;

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBreak(BlockBreakEvent event) {
		if(!isWorld(event.getPlayer())) {
			return;
		}

		if(!canInteract(event.getPlayer(), event.getBlock(), Action.LEFT_CLICK_BLOCK)) {
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

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		if(event.getItemInHand().getType().equals(Material.STONE_SHOVEL)) {
			return;
		}
		checkInteraction(event.getPlayer(), event.getBlock(), event, Action.LEFT_CLICK_BLOCK);
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(e.getClickedBlock() != null && e.getItem() != null) {
			if(e.getClickedBlock().getType().equals(Material.DIRT_PATH)) {
				if(e.getItem().getType().equals(Material.STONE_SHOVEL)) {
					e.getClickedBlock().setType(Material.GRASS_BLOCK);
					return;
				}
			}
			checkInteraction(e.getPlayer(), e.getClickedBlock(), e, e.getAction());
		}
	}

	public void checkInteraction(Player player, Block block, Cancellable event, Action action) {
//		if(player.hasPermission("rot.exclusive")) {
//			return;
//		}

		if(isWorld(player)) {
			//StringBuilder info = new StringBuilder(player.getName() + " is " + action + ": ");
			if(!canInteract(player, block, action)) {
				//info.append("not allowed");
				//System.out.println(info);
				player.sendMessage(ChatColor.RED + "This land is not owned by you or your kingdom!");
				event.setCancelled(true);
				return;
			}
			//info.append("allowed");
			//System.out.println(info);
		}
	}

	public static boolean isWorld(Player player) {
		World.Environment env = player.getWorld().getEnvironment();
		return env != World.Environment.NETHER && env != World.Environment.THE_END;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String uuid = player.getUniqueId().toString();
		if(kingData.get(uuid).isPresent()) {
			King king = kingData.get(uuid).get();
			System.out.println(deferredEventData.getAll());
			if(deferredEventData.get(uuid).isPresent()) {
				System.out.println("found ded player");
				DeferredEvent e = deferredEventData.get(uuid).get();
				Date endDate = DateUtils.addSeconds(e.getIssuedAt(), (int) (DEATH_COOLDOWN_TICKS / 20));
				Date now = new Date();
				System.out.println(now);
				System.out.println(e.getIssuedAt());
				System.out.println(endDate);
				if(now.after(endDate)) {
					System.out.println("time is after");
					king.assignedPlayer = player;
					rebirth(king);
				} else {
					if(Data.getInstance().timers.containsKey(player.getUniqueId())) {
						return;
					}
					long diffInMillies = endDate.getTime() - now.getTime();
					Data.getInstance().timers.put(player.getUniqueId(), System.currentTimeMillis() + diffInMillies);
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
							Data.getInstance().plugin, () -> rebirth(king), diffInMillies / 1000 * 20);
				}
			}
			king.assignedPlayer = player;
			king.setBossBar();
			king.setUuid(player.getUniqueId());
			king.assignedPlayer.setDisplayName(king.getFullTitle());
		}
	}


	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRespawn(PlayerRespawnEvent event) {
		Player deceasedPlayer = event.getPlayer();

		Optional<King> deceasedKing = kingData.get(deceasedPlayer.getUniqueId().toString());
		if(!deceasedKing.isPresent()) {
			return;
		}
		King king = deceasedKing.get();
		if(king.isAtWar()) {
			return;
		}

		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		deceasedPlayer.setGameMode(GameMode.SPECTATOR);
		king.changeGen();

		event.setRespawnLocation(king.homeChunk.world.getBlock(7, 150, 7).getLocation());
		DeferredEvent e = new DeferredEvent(deceasedPlayer.getUniqueId(), new Date(), DeferredEventType.DEATH);
		deferredEventData.save(e);
		scheduler.scheduleSyncDelayedTask(Data.getInstance().plugin, () -> rebirth(king), DEATH_COOLDOWN_TICKS);
		Data.getInstance().timers.put(deceasedPlayer.getUniqueId(), System.currentTimeMillis() + (DEATH_COOLDOWN_TICKS / 20 * 1000));

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

	public static void rebirth(King deceasedKing) {
		Player player = deceasedKing.assignedPlayer;
		Location highestAtCenter = player.getWorld().getHighestBlockAt(
				deceasedKing.homeChunk.world.getBlock(7, 0, 7).getLocation()).getLocation();
		highestAtCenter.add(0, 1, 0);
		player.teleport(highestAtCenter);
		player.setGameMode(GameMode.SURVIVAL);
		Data.getInstance().giveBed(player, false);
		player.sendTitle(ChatColor.GOLD + "Glory to the New King!", deceasedKing.getFullTitle(), 20, 70, 20);
		Bukkit.broadcastMessage(ChatColor.WHITE + "Glory to the New King, " + deceasedKing.getFullTitle()
				+ " of " + ChatColor.GOLD + deceasedKing.kingdomName + ".");
		player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
		EventDao deferredEventData = new DaoFactory().getDeferredEvents();
		Optional<DeferredEvent> e = deferredEventData.get(player.getUniqueId().toString());
		e.ifPresent(deferredEventData::delete);
	}

	public static boolean canInteract(Player player, Block block, Action action) {
		if(block.getY() <= 50 || block.getY() <= Data.GetHighestBlockHeightmap(block.getX(), block.getZ()) - 50) {
			//System.out.println("level is ok");
			return true;
		}

		UUID senderUUID = player.getUniqueId();
		ClaimDao claimData = new DaoFactory().getClaims();

		Optional<ClaimedChunk> claim = claimData.get(block.getChunk().toString());
		if(!claim.isPresent()) {
			if(block.getType().equals(Material.GRASS_BLOCK) || block.getType().equals(Material.DIRT_PATH)) {
				try {
					Objects.requireNonNull(player.getEquipment()).getItemInMainHand();
				} catch(NullPointerException e) {
					return false;
				}
				if(player.getEquipment().getItemInMainHand().getType().equals(Material.WOODEN_SHOVEL)) {
					return true;
				}
			}
			//System.out.println("no claim");
			if(block.getType().toString().contains("DOOR")) {
				return true;
			}
			return false;
		}

		//System.out.println("im at claim");
		ClaimedChunk chunk = claim.get();
		//claimed chunk without owner is impossible
		King kingOwner = new DaoFactory().getKings().get(chunk.owner.toString()).orElse(null);
		return chunk.owner.equals(senderUUID) || kingOwner.advisors.contains(senderUUID);

	}
}