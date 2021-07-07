package tld.sofugames.listeners;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import tld.sofugames.data.ClaimDao;
import tld.sofugames.data.DaoFactory;
import tld.sofugames.data.Data;
import tld.sofugames.data.KingDao;
import tld.sofugames.models.ClaimedChunk;
import tld.sofugames.models.King;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class PlayerMoveListener implements Listener {
	HashMap<UUID, String> lastLocation = new HashMap<>();
	HashMap<UUID, String> lastCountry = new HashMap<>();

	DaoFactory daoFactory = new DaoFactory();
	KingDao kingData = daoFactory.getKings();
	ClaimDao claimData = daoFactory.getClaims();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMove(PlayerMoveEvent event) {
		if(EventListener.isWorld(event.getPlayer())) {
			if(kingData.get(event.getPlayer().getUniqueId().toString()).isPresent()) {
				King king = kingData.get(event.getPlayer().getUniqueId().toString()).get();
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
		if(claimData.get(thisChunk.toString()).isPresent()) {
			ClaimedChunk ch = claimData.get(thisChunk.toString()).get();
			//claimed chunk always has owners
			King other = kingData.get(ch.owner.toString()).orElse(new King());
			king.contact(other);
			return !lastCountry.get(uuid).equals(other.kingdomName);
		} else {
			return !lastCountry.get(uuid).equals("Unclaimed");
		}
	}

	public void onChunkChange(King king) {
		Chunk chunk = king.assignedPlayer.getLocation().getChunk();
		lastLocation.put(king.assignedPlayer.getUniqueId(), chunk.toString());
		if(isCountryChanged(king)) {
			if(claimData.get(chunk.toString()).isPresent()) {
				ClaimedChunk lastChunk = claimData.get(chunk.toString()).get();
				lastCountry.put(king.assignedPlayer.getUniqueId(), kingData
						.get(lastChunk.owner.toString()).orElse(new King()).kingdomName);
				king.getKingdomBar().setColor(BarColor.PURPLE);
				king.getKingdomBar().setTitle(kingData
						.get(lastChunk.owner.toString()).orElse(new King()).kingdomName);
			} else {
				lastCountry.put(king.assignedPlayer.getUniqueId(), "Unclaimed");
				king.getKingdomBar().setColor(BarColor.GREEN);
				king.getKingdomBar().setTitle("Unclaimed");
			}
			showBar(king);
		}
		if(king.assignedPlayer.getGameMode() == GameMode.SPECTATOR) {
			king.assignedPlayer.teleport(king.homeChunk.world.getBlock(7, 150, 7).getLocation());
			king.assignedPlayer.sendMessage(ChatColor.RED + "You can't leave your home chunk while dead.");
		}
	}

	public void showBar(King king) {
		king.getKingdomBar().setVisible(true);
		if(!king.barSetToCancel) {
			king.barSetToCancel = true;
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Objects.requireNonNull(Bukkit.getPluginManager().getPlugins()[0]),
					() -> hideBar(king), 100L);
		}
	}

	public void hideBar(King king) {
		king.barSetToCancel = false;
		king.getKingdomBar().setVisible(false);
	}
}
