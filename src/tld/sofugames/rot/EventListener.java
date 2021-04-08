package tld.sofugames.rot;

import net.minecraft.server.v1_16_R3.DoubleBlockFinder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import tld.sofugames.data.*;
import tld.sofugames.models.*;

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
					int space = allDirectionSearch(start, new HashMap<>(), 0);
					if (space != 0) {
						event.getPlayer().sendMessage(ChatColor.GOLD + "House claimed! Space: " + space);
					} else {
						event.getPlayer().sendMessage(ChatColor.RED + "This house can't match the rules!");
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

	public int allDirectionSearch(Block currentBlock, HashMap<String, Block> visitedList, int space) {
		BlockFace[] faces = new BlockFace[]{BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN};
		for (BlockFace face : faces) {
			Block rel = currentBlock.getRelative(face);
			if (!visitedList.containsKey(rel.toString())) {
				visitedList.put(rel.toString(), rel);
				if (rel.getType() == Material.AIR) {
					space++;
					if (space >= 100) {
						return 0;
					}
					int temp = allDirectionSearch(rel, visitedList, space);
					if (temp == 0) {
						return 0;
					} else {
						space = temp;
					}
				}
			}
		}
		return space;
	}
}