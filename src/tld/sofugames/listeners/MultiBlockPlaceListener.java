package tld.sofugames.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import tld.sofugames.data.Data;
import tld.sofugames.models.House;
import tld.sofugames.rot.HousingOutOfBoundsException;

import java.sql.SQLException;
import java.util.HashMap;

public class MultiBlockPlaceListener implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMultiBlockPlace(BlockMultiPlaceEvent event) {
		if(EventListener.isWorld(event.getPlayer())) {
			if(EventListener.checkOwnership(event.getPlayer(), event.getBlock())) {
				Block start = event.getBlock();
				if(start.getBlockData() instanceof org.bukkit.block.data.type.Bed) {
					if(hasCeiling(start, 0)) {
						House newHouse = new House(Data.getInstance().getLastHouse(),
								event.getPlayer().getUniqueId(), Data.getInstance().getBedHash(start));
						try {
							newHouse = allDirectionSearch(start, new HashMap<>(), newHouse, null);

							event.getPlayer().sendMessage(ChatColor.GOLD + "House claimed!");

							newHouse.pushToDb(Data.getInstance().getConnection());

							Data.getInstance().houseData.put(newHouse.bedBlock, newHouse);

							Data.getInstance().giveBed(event.getPlayer(), false);
						} catch(HousingOutOfBoundsException | SQLException e) {
							event.getPlayer().sendMessage(ChatColor.RED + e.getMessage());
							event.setCancelled(true);
						}
					} else {
						event.getPlayer().sendMessage(ChatColor.RED + "This house can't match the rules!");
						event.setCancelled(true);
					}
				}
			} else {
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED + "This land is not owned by you or your kingdom!");
			}
		}
	}



	public boolean hasCeiling(Block current, int counter) {
		if (counter > 15) return false;

		if (current.getRelative(BlockFace.UP).getType() == Material.AIR) {
			counter++;
			return hasCeiling(current.getRelative(BlockFace.UP), counter);
		} else {
			return true;
		}
	}

	public House allDirectionSearch(Block currentBlock, HashMap<String, Block> visitedList, House thisHouse, Block startBed)
			throws HousingOutOfBoundsException {
		if (startBed == null) startBed = currentBlock;
		for (BlockFace face : Data.getInstance().faces) {
			Block rel = currentBlock.getRelative(face);
			if (!visitedList.containsKey(rel.toString())) {
				visitedList.put(rel.toString(), rel);
				if (rel.getType() == Material.AIR || rel.getType() == Material.TORCH //*********************************
						|| Data.getInstance().ignoreList.contains(rel.getType())) {
					if (Tag.BEDS.getValues().contains(rel.getType())) {
						if (Data.getInstance().houseData.containsKey(Data.getInstance().getBedHash(rel))) {
							throw new HousingOutOfBoundsException("House is already claimed!");
						}
					}
					thisHouse.area++;
					if (thisHouse.area > 150) {
						throw new HousingOutOfBoundsException("House area doesn't match the rules: Size can't be > 150");
					}
					House tempHouse = allDirectionSearch(rel, visitedList, thisHouse, startBed);
					if (tempHouse.area == 0) {
						throw new HousingOutOfBoundsException("House area doesn't match the rules: Size can't be > 150");
					} else {
						thisHouse.area = tempHouse.area;
					}
				} else if (Data.getInstance().benefitList.contains(rel.getType())) {
					thisHouse.benefits++;
				}
			}
		}
		return thisHouse;
	}
}
