package tld.sofugames.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tld.sofugames.data.Data;
import tld.sofugames.gui.WarGui;
import tld.sofugames.models.War;
import tld.sofugames.rot.WarType;

import java.util.Arrays;

public class WarGuiListener implements Listener {

	// Check for clicks on items
	@EventHandler
	public void onInventoryClick(final InventoryClickEvent e) {
		if(e.getClickedInventory() == null) { return; }
		if(e.getInventory() instanceof WarGui) {
			e.setCancelled(true);
			final ItemStack clickedItem = e.getCurrentItem();
			if(clickedItem == null || clickedItem.getType().isAir()) return;
			final Player p = (Player) e.getWhoClicked();
			p.sendMessage("You clicked at slot " + e.getRawSlot());
			War thisWar = Data.getInstance().wars.get(p.getUniqueId().toString());
			thisWar.setWarType(WarType.types[e.getRawSlot() - 2]);
			p.closeInventory();
			thisWar.getAtk().assignedPlayer.sendTitle(ChatColor.RED + "WAR!", "", 20, 70, 20);
			thisWar.getAtk().assignedPlayer.playSound(thisWar.getAtk().assignedPlayer.getLocation(),
					Sound.ENTITY_PILLAGER_CELEBRATE, 1, 1);
			thisWar.getDef().assignedPlayer.sendTitle(ChatColor.RED + "WAR!",
					"King " + thisWar.getDef().fullTitle + " declares war on us!", 20, 70, 20);
			thisWar.getDef().assignedPlayer.playSound(thisWar.getDef().assignedPlayer.getLocation(),
					Sound.EVENT_RAID_HORN, 1, 1);
		}
	}

	@EventHandler
	public void onInventoryClose(final InventoryCloseEvent e) {
		if(e.getInventory() instanceof WarGui) {
			String atkUuid = e.getPlayer().getUniqueId().toString();
			if(Data.getInstance().wars.containsKey(atkUuid)) {
				War war = Data.getInstance().wars.get(atkUuid);
				if(war.getWarType() == null) {
					Data.getInstance().destroyWar(atkUuid);
				}
			}
		}
	}
//	// Cancel dragging in our inventory
//	@EventHandler
//	public void onInventoryClick(final InventoryDragEvent e) {
//		if(e.get() == null) { return; }
//		if(e.getInventory() instanceof WarGui) {
//			e.setCancelled(true);
//		}
//	}
}
