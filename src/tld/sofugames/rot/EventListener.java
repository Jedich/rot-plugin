package tld.sofugames.rot;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import tld.sofugames.data.*;
import tld.sofugames.models.*;

import java.util.*;

public class EventListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		checkOwnership(event, event.getPlayer(), event.getBlock());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		checkOwnership(event, event.getPlayer(), event.getBlock());
	}
	
	public void checkOwnership(Cancellable event, Player player, Block block) {
		UUID senderUUID = player.getUniqueId();
		ClaimedChunk chunk = Data.getInstance().claimData.get(block.getChunk().toString());
		if (block.getY() > 40) {
			if (chunk != null) {
				if (!chunk.owner.equals(senderUUID)) {
					System.out.println(chunk.owner + " " + senderUUID);
					event.setCancelled(true);
					player.sendMessage(ChatColor.RED + "This land is not owned by you or your kingdom!");
				}
			}
			else {
				event.setCancelled(true);
				player.sendMessage(ChatColor.RED + "This land is not owned by you or your kingdom!");
			}
		}
	}

}