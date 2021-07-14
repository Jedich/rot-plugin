package tld.sofugames.listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import tld.sofugames.dao.impl.ClaimDao;
import tld.sofugames.dao.impl.DaoFactory;
import tld.sofugames.dao.impl.KingDao;
import tld.sofugames.dao.impl.WarDao;
import tld.sofugames.data.*;
import tld.sofugames.gui.WarGui;
import tld.sofugames.models.King;
import tld.sofugames.models.War;
import tld.sofugames.rot.WarType;

public class WarGuiListener implements Listener {

	DaoFactory daoFactory = new DaoFactory();
	KingDao kingData = daoFactory.getKings();
	ClaimDao claimData = daoFactory.getClaims();
	WarDao wars = daoFactory.getWars();

	// Check for clicks on items
	@EventHandler
	public void onInventoryClick(final InventoryClickEvent e) {
		if(e.getClickedInventory() == null) { return; }
		if(e.getClickedInventory().getHolder() instanceof WarGui) {
			e.setCancelled(true);
			final ItemStack clickedItem = e.getCurrentItem();
			if(clickedItem == null || clickedItem.getType().isAir()) return;
			final Player p = (Player) e.getWhoClicked();
			//p.sendMessage("You clicked at slot " + e.getRawSlot());
			War thisWar = wars.get(p.getUniqueId().toString()).get();
			thisWar.setWarType(WarType.types[e.getRawSlot() - 2]);
			p.closeInventory();
			thisWar.updateWarState(true);
			thisWar.getAtk().assignedPlayer.sendTitle("§4WAR!", "", 20, 70, 20);
			thisWar.getAtk().assignedPlayer.playSound(thisWar.getAtk().assignedPlayer.getLocation(),
					Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1, 1);
			thisWar.getDef().assignedPlayer.sendTitle("§4WAR!",
					"King " + thisWar.getAtk().getFullTitle() + " §cdeclares war on us!", 20, 70, 20);
			thisWar.getDef().assignedPlayer.playSound(thisWar.getDef().assignedPlayer.getLocation(),
					Sound.EVENT_RAID_HORN, 1, 1);
			thisWar.getDef().assignedPlayer.playSound(thisWar.getDef().assignedPlayer.getLocation(),
					Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1, 1);
			Data.getInstance().plugin.getServer().broadcastMessage("§cThe wind is rising...");
			System.out.println(thisWar.getAtk().assignedPlayer.getName() + " declares war on "
					+ thisWar.getDef().assignedPlayer.getName());
			wars.save(thisWar);
			for(King ally : thisWar.getDef().allies) {
				ally.assignedPlayer.sendMessage("One of your allies is now fighting in a defensive war! You can join with §6/war join");
			}
			for(King ally : thisWar.getAtk().allies) {
				ally.assignedPlayer.sendMessage("One of your allies is now fighting in an aggressive war! You can join with §6/war join");
			}
		}
	}

	@EventHandler
	public void onInventoryClose(final InventoryCloseEvent e) {
		System.out.println(e.getInventory().getHolder());
		if(e.getInventory().getHolder() instanceof WarGui) {
			String atkUuid = e.getPlayer().getUniqueId().toString();
			if(wars.get(atkUuid).isPresent()) {
				War war = wars.get(atkUuid).get();
				if(war.getWarType() == null) {
					war.updateWarState(false);
				}
			}
		}
	}
}
