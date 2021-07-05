package tld.sofugames.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import tld.sofugames.data.Data;
import tld.sofugames.models.King;
import tld.sofugames.models.War;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PlayerDeathListener implements Listener {

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		HashMap<String, King> kingData = Data.getInstance().kingData;
		if(!kingData.containsKey(event.getEntity().getUniqueId().toString())) return;
		King victim = kingData.get(event.getEntity().getUniqueId().toString());
		if(victim.isAtWar() && event.getEntity().getKiller() != null) {
			event.setDroppedExp(20);
			event.setKeepInventory(true);
			event.getDrops().clear();
			if(kingData.containsKey(event.getEntity().getKiller().getUniqueId().toString())) {
				King killer = kingData.get(event.getEntity().getKiller().getUniqueId().toString());
				if(!killer.isAtWar()) return;
				War thisWar = killer.getCurrentWar();
				if(victim.equals(thisWar.getDef()) || victim.equals(thisWar.getAtk())) {
					thisWar.changeWarScore(victim.equals(thisWar.getDef()) || thisWar.atkAllies.contains(killer));
					killer.assignedPlayer.sendTitle(ChatColor.GOLD + "Battle won!", "", 20, 70, 20);
					killer.assignedPlayer.playSound(killer.assignedPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
				}
			}
		} else {
			if(event.getKeepInventory()) {
				event.setKeepInventory(false);
			}
		}
	}
}
