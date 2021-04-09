package tld.sofugames.rot;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import tld.sofugames.data.*;
import tld.sofugames.models.*;

import java.sql.SQLException;
import java.util.*;

public class EventListener implements Listener {

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockBreak(BlockBreakEvent event) {
		if (!checkOwnership(event.getPlayer(), event.getBlock())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "This land is not owned by you or your kingdom!");
		} else {
			if (Tag.BEDS.getValues().contains(event.getBlock().getType())) {
				if (Data.getInstance().houseData.containsKey(Data.getInstance().getBedHash(event.getBlock()))) {
					try {
						event.getPlayer().sendMessage("Bed was destroyed");
						Data.getInstance().houseData.get(Data.getInstance().getBedHash(event.getBlock())).Delete(Data.getInstance().connection);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (!checkOwnership(event.getPlayer(), event.getBlock())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "This land is not owned by you or your kingdom!");
		}
	}


	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMultiBlockPlace(BlockMultiPlaceEvent event) {
		if (checkOwnership(event.getPlayer(), event.getBlock())) {
			Block start = event.getBlock();
			if (start.getBlockData() instanceof org.bukkit.block.data.type.Bed) {
				if (hasCeiling(start, 0)) {
					House newHouse = new House(Data.getInstance().getLastHouse(), event.getPlayer().getUniqueId(), Data.getInstance().getBedHash(start));
					System.out.println(Data.getInstance().getBedHash(start));
					try {
						newHouse = allDirectionSearch(start, new HashMap<>(), newHouse, null);

						event.getPlayer().sendMessage(ChatColor.GOLD + "House claimed!");

						Data.getInstance().giveBed(event.getPlayer());

						newHouse.pushToDb(Data.getInstance().connection);

						Data.getInstance().houseData.put(newHouse.bedBlock, newHouse);
					} catch (HousingOutOfBoundsException | SQLException e) {
						event.getPlayer().sendMessage(ChatColor.RED + e.getMessage());
						start.breakNaturally();
					}
				} else {
					event.getPlayer().sendMessage(ChatColor.RED + "This house can't match the rules!");
					start.breakNaturally();
				}
			}
		} else {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "This land is not owned by you or your kingdom!");
		}
	}

	public boolean checkOwnership(Player player, Block block) {
		UUID senderUUID = player.getUniqueId();
		ClaimedChunk chunk = Data.getInstance().claimData.get(block.getChunk().toString());
		if (block.getY() > 40) {
			if (chunk != null) {
				if (!chunk.owner.equals(senderUUID)) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
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