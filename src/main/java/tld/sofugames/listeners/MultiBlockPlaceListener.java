package tld.sofugames.listeners;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import tld.sofugames.dao.impl.DaoFactory;
import tld.sofugames.dao.impl.HouseDao;
import tld.sofugames.data.*;
import tld.sofugames.models.House;
import tld.sofugames.rot.HousingOutOfBoundsException;

public class MultiBlockPlaceListener implements Listener {

	DaoFactory daoFactory = new DaoFactory();
	HouseDao houseData = daoFactory.getHouses();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMultiBlockPlace(BlockMultiPlaceEvent event) {
		if(EventListener.isWorld(event.getPlayer())) {
			if(EventListener.canInteract(event.getPlayer(), event.getBlock())) {
				Block start = event.getBlock();
				if(start.getBlockData() instanceof org.bukkit.block.data.type.Bed) {
					House newHouse = new House(event.getPlayer().getUniqueId(), Data.getInstance().getBedHash(start), start);
					try {
						if(newHouse.isEnclosed()) {
							event.getPlayer().sendMessage(ChatColor.GOLD + "House claimed!");
							houseData.save(newHouse);
							newHouse.calculateIncome();
							Data.getInstance().giveBed(event.getPlayer(), false);
						}
						else {
							event.getPlayer().sendMessage(ChatColor.RED + "House size must be 2 > size > 150! \n Check if it's enclosed.");
							event.setCancelled(true);
						}
					} catch(HousingOutOfBoundsException e) {
						event.getPlayer().sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "House was already claimed.");
						Data.getInstance().giveBed(event.getPlayer(), false);
					}
				}
			} else {
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED + "This land is not owned by you or your kingdom!");
			}
		}
	}


}
