package tld.sofugames.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class WarGui implements InventoryHolder {
	private Inventory inv;

	public WarGui() {
		// Create a new inventory, with no owner (as this isn't a real inventory), a size of nine, called example
		inv = Bukkit.createInventory(this, 9, "Choose a war type");
		// Put the items into the inventory
		initializeItems();
	}

	// You can call this whenever you want to put the items in
	private void initializeItems() {
		inv.setItem(2, createGuiItem(Material.STICK, "§eHumiliation",
				"§7Show how weak they are.", "§eMedium (50%)"));
		inv.setItem(3, createGuiItem(Material.WOODEN_SWORD, "§eRaid",
				"§7They will be great suppliers to our treasury.", "§aEasy (40%)"));
		inv.setItem(4, createGuiItem(Material.IRON_SWORD, "§eExpansion",
				"§7They have some land of ours that must be returned.", "§eMedium (60%)"));
		inv.setItem(5, createGuiItem(Material.SHIELD, "§eVassalisation",
				"§7They must be 'protected' by us.", "§cHard (100%)"));
		inv.setItem(6, createGuiItem(Material.GOLDEN_SWORD, "§eConquest",
				"§7All their land are belong to us!", "§cHard (100%)"));
	}

	// Nice little method to create a gui item with a custom name, and description
	protected ItemStack createGuiItem(final Material material, final String name, final String... lore) {
		final ItemStack item = new ItemStack(material, 1);
		final ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		meta.setLore(Arrays.asList(lore));
		item.setItemMeta(meta);
		return item;
	}

	// You can open the inventory with this
	public void openInventory(final HumanEntity ent) {
		ent.openInventory(inv);
	}

	@Override
	public Inventory getInventory() {
		return inv;
	}
}
