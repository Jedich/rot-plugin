package tld.sofugames.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import tld.sofugames.dao.impl.DaoFactory;
import tld.sofugames.dao.impl.KingDao;
import tld.sofugames.models.King;
import tld.sofugames.models.War;

public class PlayerDeathListener implements Listener {

	DaoFactory daoFactory = new DaoFactory();
	KingDao kingData = daoFactory.getKings();

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if(!kingData.get(event.getEntity().getUniqueId().toString()).isPresent()) {
			return;
		}
		King victim = kingData.get(event.getEntity().getUniqueId().toString()).get();
		if(victim.isAtWar() && event.getEntity().getKiller() != null) {
			event.setDroppedExp(20);
			event.setKeepInventory(true);
			event.getDrops().clear();
			String killerUuid = event.getEntity().getKiller().getUniqueId().toString();
			if(kingData.get(killerUuid).isPresent()) {
				King killer = kingData.get(killerUuid).get();
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
