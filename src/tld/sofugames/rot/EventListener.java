package tld.sofugames.rot;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.inventory.ItemStack;
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
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (checkOwnership(event.getPlayer(), event.getBlock())) {
			Block start = event.getBlock();
			if (start.getBlockData() instanceof org.bukkit.block.data.type.Bed) {
				if (hasCeiling(start, 0)) {
					House newHouse = new House(event.getPlayer().getUniqueId());
					try {
						newHouse = allDirectionSearch(start, new HashMap<>(), newHouse, null);

						event.getPlayer().sendMessage(ChatColor.GOLD + "House claimed! Space: "
								+ newHouse.area + "\nbenefits:" + newHouse.benefits);

						event.getPlayer().getInventory().addItem(
								new ItemStack(Tag.BEDS.getValues().
										stream()
										.skip(new Random().nextInt(
												Tag.BEDS.getValues().size())).findFirst().orElse(Material.WHITE_BED)));

						newHouse.pushToDb(Data.getInstance().connection);
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


	@EventHandler(priority = EventPriority.HIGHEST)
	public void houseClaim(BlockPlaceEvent event) {

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
		if(startBed == null) startBed = currentBlock;
		for (BlockFace face : Data.getInstance().faces) {
			Block rel = currentBlock.getRelative(face);
			if (!visitedList.containsKey(rel.toString())) {
				visitedList.put(rel.toString(), rel);
				if (rel.getType() == Material.AIR || rel.getType() == Material.TORCH //*********************************
						|| Data.getInstance().ignoreList.contains(rel.getType())) {
					if(Tag.BEDS.getValues().contains(rel.getType())) {
						if(currentBlock != rel) {
							throw new HousingOutOfBoundsException("House area doesn't match the rules");
						}
					}
					thisHouse.area++;
					if (thisHouse.area > 150) {
						throw new HousingOutOfBoundsException("House area doesn't match the rules");
					}
					House tempHouse = allDirectionSearch(rel, visitedList, thisHouse, startBed);
					if (tempHouse.area == 0) {
						throw new HousingOutOfBoundsException("House area doesn't match the rules");
					} else {
						thisHouse.area = tempHouse.area;
					}
				} else if(Data.getInstance().benefitList.contains(rel.getType())) {
					thisHouse.benefits++;
				}
			}
		}
		return thisHouse;
	}
}