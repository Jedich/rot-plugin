package tld.sofugames.listeners;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import tld.sofugames.data.Data;
import tld.sofugames.models.House;
import tld.sofugames.rot.HousingOutOfBoundsException;

import java.sql.SQLException;

public class MultiBlockPlaceListener implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMultiBlockPlace(BlockMultiPlaceEvent event) {
		if(EventListener.isWorld(event.getPlayer())) {
			if(EventListener.checkOwnership(event.getPlayer(), event.getBlock())) {
				Block start = event.getBlock();
				if(start.getBlockData() instanceof org.bukkit.block.data.type.Bed) {
					House newHouse = new House(Data.getInstance().getLastHouse(),
							event.getPlayer().getUniqueId(), Data.getInstance().getBedHash(start), start);
					try {
						if(newHouse.isEnclosed()) {
							event.getPlayer().sendMessage(ChatColor.GOLD + "House claimed!");

							newHouse.pushToDb(Data.getInstance().getConnection());

							Data.getInstance().houseData.put(newHouse.bedBlockId, newHouse);

							Data.getInstance().giveBed(event.getPlayer(), false);
						}
						else {
							event.getPlayer().sendMessage(ChatColor.RED + "House size must be 2 > size > 150! \n Check if it's enclosed.");
							event.setCancelled(true);
						}
					} catch(HousingOutOfBoundsException e) {
						event.getPlayer().sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "House was already claimed.");
						Data.getInstance().giveBed(event.getPlayer(), false);
					} catch(SQLException e) {
						event.getPlayer().sendMessage(ChatColor.RED + "Internal server error.");
						e.printStackTrace();
						event.setCancelled(true);
					}
				}
			} else {
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED + "This land is not owned by you or your kingdom!");
			}
		}
	}


}
