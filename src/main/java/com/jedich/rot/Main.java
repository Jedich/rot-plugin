package com.jedich.rot;

import com.jedich.commands.*;
import com.jedich.dao.impl.*;
import com.jedich.data.Data;
import com.jedich.dynmap.Dynmap;
import com.jedich.listeners.EventListener;
import com.jedich.listeners.*;
import com.jedich.models.ClaimedChunk;
import com.jedich.models.House;
import com.jedich.models.King;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.MarkerSet;

import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;


public class Main extends JavaPlugin {

	static Connection connection;
	DaoFactory daoFactory = new DaoFactory();
	KingDao kingData = daoFactory.getKings();
	ClaimDao claimData = daoFactory.getClaims();
	HouseDao houseData = daoFactory.getHouses();
	WarDao wars = daoFactory.getWars();

	@Override
	public void onEnable() {
		Data data = Data.getInstance();
		data.setPlugin(this);
		connection = data.getConnection();

		getCommand("claim").setExecutor(new ClaimCommand());
		getCommand("unclaim").setExecutor(new UnclaimCommand());
		getCommand("accept").setExecutor(new AcceptCommand());
		KingdomCommand k = new KingdomCommand();
		getCommand("kingdom").setExecutor(k);
		getCommand("kingdom").setTabCompleter(k.new PluginTabCompleter());
		DiploCommand d = new DiploCommand();
		getCommand("diplomacy").setExecutor(d);
		getCommand("diplomacy").setTabCompleter(d.new PluginTabCompleter());
		WarCommand w = new WarCommand();
		getCommand("war").setExecutor(w);
		getCommand("war").setTabCompleter(w.new PluginTabCompleter());
		getCommand("rot-respawn").setTabCompleter(this.new PluginTabCompleter());


		getServer().getPluginManager().registerEvents(new EventListener(), this);
		getServer().getPluginManager().registerEvents(new MultiBlockPlaceListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);
		getServer().getPluginManager().registerEvents(new WarGuiListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);


		Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "time set 0");
		BukkitScheduler scheduler = getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, this::checkIncomes, 24000L, 24000L);
		scheduler.scheduleSyncRepeatingTask(this, this::checkHouses, 0, 6000L);

		scheduler.scheduleSyncDelayedTask(Data.getInstance().plugin, () -> registerDynmap(this), 100L);
		Data.LoadHeightmap(this);
		wars.getAll();

	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("reset")) {
			restart();
			return true;
		} else if(command.getName().equalsIgnoreCase("chunk-info")) {
			Player player = getServer().getPlayer(sender.getName());
			String chunkName = player.getLocation().getChunk().toString();
			String ownerName = "None";
			sender.sendMessage("Current chunk: " + chunkName);
			if(claimData.get(chunkName).isPresent()) {
				sender.sendMessage("Current owner: " +
						new DaoFactory().getKings().get(claimData.get(chunkName).get().owner.toString()).get().getFullTitle());
			} else {
				sender.sendMessage("The chunk is available for conquest!");
			}
			return true;
		} else if(command.getName().equalsIgnoreCase("timeleft")) {
			if(((Player) sender).getGameMode() != GameMode.SPECTATOR) {
				sender.sendMessage(ChatColor.ITALIC + "You are alive... breathing, at least.");
				return true;
			}
			if(!Data.getInstance().timers.containsKey(((Player) sender).getUniqueId())) {
				sender.sendMessage(ChatColor.ITALIC + "Unable to find your death story...");
				return true;
			}

			long time = (Data.getInstance().timers.get(((Player) sender).getUniqueId())
					- System.currentTimeMillis()) / 1000;
			int minutes = 0;
			while(time >= 60) {
				time -= 60;
				minutes++;
			}

			sender.sendMessage(ChatColor.AQUA + "" + minutes + "m " + time + "s left until respawn...");
			return true;
		} else if(command.getName().equalsIgnoreCase("rot-respawn")) {
			EventListener.rebirth(kingData.get(((Player) sender).getUniqueId().toString()).orElse(new King()));
		}
		return false;
	}

	public class PluginTabCompleter implements TabCompleter {
		@Override
		public List<String> onTabComplete(CommandSender sender, Command cde, String arg, String[] args) {
			if(args.length > 0) {
				return Bukkit.getServer().getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
			} else return Collections.emptyList();
		}
	}

	public void restart() {
		claimData.getAll().clear();
		kingData.getAll().clear();
		houseData.getAll().clear();
		wars.getAll().clear();
		onDisable();
		connection = Data.getInstance().getConnection();
		onEnable();
	}


	@Override
	public void onDisable() {
		wars.updateAll();
		try {
			if(connection != null && !connection.isClosed()) {
				connection.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		HandlerList.unregisterAll();
		getServer().getScheduler().cancelTasks(this);
	}

	public void checkHouses() {
		int incorrectTotal = 0;
		HashMap<UUID, Integer> incorrectBeds = new HashMap<>();
		Iterator<Map.Entry<String, House>> iterator = houseData.getAll().entrySet().iterator();
		while(iterator.hasNext()) {
			Map.Entry<String, House> houseSet = iterator.next();
			House house = (House) houseSet.getValue();
			if(kingData.get(house.owner.toString()).orElse(new King()).assignedPlayer != null) {
				Player player = kingData.get(house.owner.toString()).get().assignedPlayer;
				if(!player.isOnline()) {
					continue;
				}
			} else {
				continue;
			}
			try {
				if(!house.isEnclosed()) {
					if(incorrectBeds.containsKey(house.owner)) {
						incorrectBeds.put(house.owner, incorrectBeds.get(house.owner) + 1);
					} else {
						incorrectBeds.put(house.owner, 1);
					}
					houseData.delete(house);
					house.bedBlock.breakNaturally();
					iterator.remove();
					incorrectTotal++;
				}
			} catch(HousingOutOfBoundsException e) {
				e.printStackTrace();
			}
		}
		for(Map.Entry<UUID, Integer> entry : incorrectBeds.entrySet()) {
			if(kingData.get(entry.getKey().toString()).orElse(new King()).assignedPlayer != null) {
				Player player = kingData.get(entry.getKey().toString()).get().assignedPlayer;
				if(player.isOnline()) {
					player.sendMessage(ChatColor.RED + "Found " + entry.getValue() + " incorrect beds! They were destroyed.");
				}
			}
		}
		System.out.println("Found total " + incorrectTotal + " incorrect beds.");
	}

	public void checkIncomes() {
		for(King king : kingData.getAll().values()) {
			if(king.assignedPlayer != null) {
				if(Bukkit.getPlayerExact(king.assignedPlayer.getName()) != null) {
					king.setGoldBalance(king.getIncome() - king.getFee());

					king.assignedPlayer.sendMessage(ChatColor.GOLD + "Good morning, my honor!");
					king.assignedPlayer.sendMessage("Income: " + ChatColor.GREEN +
							String.format(Locale.US, "%.1f", king.getIncome()) + "ingots. " +
							ChatColor.WHITE + "Charge: " + ChatColor.RED +
							String.format(Locale.US, "%.1f", king.getFee()) + " ingots.");
					king.assignedPlayer.sendMessage("Your balance: " + ChatColor.GOLD +
							String.format(Locale.US, "%.1f", king.getGoldBalance()) + "ingots.");
					if(king.getGoldBalance() < -5 && king.getGoldBalance() > -20) {
						king.assignedPlayer.playSound(king.assignedPlayer.getLocation(),
								Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
						king.assignedPlayer.sendMessage(ChatColor.DARK_RED + "The treasury is empty, my lord! " +
								"We should take a foreign aid before it's not too late!");
					} else if(king.getGoldBalance() <= -20) {
						king.assignedPlayer.sendMessage(ChatColor.DARK_RED + "Pillagers " +
								ChatColor.RED + "have come to end your pathetic suffering.");
						Location loc = king.homeChunk.world.getBlock(1, king.assignedPlayer.getWorld()
								.getHighestBlockYAt(king.homeChunk.world.getX() * 16 + 1,
										king.homeChunk.world.getZ() * 16 + 1), 1).getLocation();
						World world = king.assignedPlayer.getWorld();
						for(int i = 0; i < 20; i++) {
							world.spawnEntity(loc, EntityType.PILLAGER);
						}
						king.assignedPlayer.playSound(king.assignedPlayer.getLocation(),
								Sound.ENTITY_WITHER_HURT, 1, 1);
					} else {
						king.assignedPlayer.playSound(king.assignedPlayer.getLocation(),
								Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
					}
				}
			}
		}
	}

	public static void registerDynmap(JavaPlugin p) {
		Dynmap.dapi = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("dynmap");
		if(Dynmap.dapi == null) {
			Bukkit.getServer().getPluginManager().disablePlugin(p);
		}

		for(MarkerSet ms : Dynmap.dapi.getMarkerAPI().getMarkerSets()) {
			ms.deleteMarkerSet();
		}

		DaoFactory daoFactory = new DaoFactory();
		KingDao kingData = daoFactory.getKings();
		for(King king : kingData.getAll().values()) {
			Dynmap.addKing(king);
		}

		ClaimDao claimData = daoFactory.getClaims();
		for(ClaimedChunk chunk : claimData.getAll().values()) {
			Dynmap.paintChunk(chunk);
		}

	}

}