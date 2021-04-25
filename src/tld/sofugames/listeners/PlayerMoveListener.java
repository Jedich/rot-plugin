package tld.sofugames.listeners;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import tld.sofugames.data.Data;
import tld.sofugames.models.King;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class PlayerMoveListener implements Listener {
	HashMap<UUID, String> lastLocation = new HashMap<>();
	HashMap<UUID, String> lastCountry = new HashMap<>();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMove(PlayerMoveEvent event) {
		if(EventListener.isWorld(event.getPlayer())) {
			if(Data.getInstance().kingData.containsKey(event.getPlayer().getUniqueId().toString())) {
				King king = Data.getInstance().kingData.get(event.getPlayer().getUniqueId().toString());
				if(isChanged(king)) {
					onChunkChange(king);
				}
			}
		}
	}

	public boolean isChanged(King king) {
		UUID uuid = king.assignedPlayer.getUniqueId();
		if(!lastLocation.containsKey(uuid)) {
			return true;
		}
		return !lastLocation.get(uuid).equals(king.assignedPlayer.getLocation().getChunk().toString());
	}

	public boolean isCountryChanged(King king) {
		UUID uuid = king.assignedPlayer.getUniqueId();
		Chunk thisChunk = king.assignedPlayer.getLocation().getChunk();
		if(!lastCountry.containsKey(uuid)) {
			return true;
		}
		if(Data.getInstance().claimData.containsKey(thisChunk.toString())) {
			return !lastCountry.get(uuid).equals(Data.getInstance().kingData
					.get(Data.getInstance().claimData.get(thisChunk.toString()).owner.toString()).kingdomName);
		} else {
			return !lastCountry.get(uuid).equals("Unclaimed");
		}
	}

	public void onChunkChange(King king) {
		Chunk chunk = king.assignedPlayer.getLocation().getChunk();
		lastLocation.put(king.assignedPlayer.getUniqueId(), chunk.toString());
		if(isCountryChanged(king)) {
			if(Data.getInstance().claimData.containsKey(chunk.toString())) {
				lastCountry.put(king.assignedPlayer.getUniqueId(), Data.getInstance().kingData
						.get(Data.getInstance().claimData.get(chunk.toString()).owner.toString()).kingdomName);
				king.kingdomBar.setColor(BarColor.PURPLE);
				king.kingdomBar.setTitle(Data.getInstance().kingData
						.get(Data.getInstance().claimData.get(chunk.toString()).owner.toString()).kingdomName);
			} else {
				lastCountry.put(king.assignedPlayer.getUniqueId(), "Unclaimed");
				king.kingdomBar.setColor(BarColor.GREEN);
				king.kingdomBar.setTitle("Unclaimed");
			}
			showBar(king);
		}
		if(king.assignedPlayer.getGameMode() == GameMode.SPECTATOR) {
			king.assignedPlayer.teleport(king.homeChunk.world.getBlock(7, 150, 7).getLocation());
			king.assignedPlayer.sendMessage(ChatColor.RED + "You can't leave your home chunk while dead.");
		}
	}

	public void showBar(King king) {
		king.kingdomBar.setVisible(true);
		if(!king.barSetToCancel) {
			king.barSetToCancel = true;
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Objects.requireNonNull(Bukkit.getPluginManager().getPlugins()[0]),
					() -> hideBar(king), 100L);
		}
	}

	public void hideBar(King king) {
		king.barSetToCancel = false;
		king.kingdomBar.setVisible(false);
	}
}
