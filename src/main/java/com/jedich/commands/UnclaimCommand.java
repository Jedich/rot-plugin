package com.jedich.commands;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.jedich.dao.impl.ClaimDao;
import com.jedich.dao.impl.DaoFactory;
import com.jedich.dao.impl.KingDao;
import com.jedich.dynmap.Dynmap;
import com.jedich.models.ClaimedChunk;
import com.jedich.models.King;
import com.jedich.rot.ChunkType;

import java.util.HashMap;
import java.util.UUID;

public class UnclaimCommand implements CommandExecutor {

	public HashMap<UUID, King> requests = new HashMap<>();
	DaoFactory daoFactory = new DaoFactory();
	KingDao kingData = daoFactory.getKings();
	ClaimDao claimData = daoFactory.getClaims();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("unclaim")) {
			Player player = ((Player) sender);
			if(!kingData.get(player.getUniqueId().toString()).isPresent()) {
				sender.sendMessage(ChatColor.RED + "You are not a king to perform this command.");
				return true;
			}
			Chunk targetChunk = player.getLocation().getChunk();
			if(!claimData.get(targetChunk.toString()).isPresent()) {
				sender.sendMessage(ChatColor.RED + "This chunk is not claimed.");
				return true;
			}
			ClaimedChunk claim = claimData.get(targetChunk.toString()).get();
			if(!claim.owner.equals(player.getUniqueId())) {
				sender.sendMessage(ChatColor.RED + "This is not your kingdoms' chunk!");
				return true;
			}
			if(claim.type == ChunkType.Home) {
				sender.sendMessage(ChatColor.RED + "Can't unclaim the home chunk!");
				return true;
			}
			claimData.delete(claim);
			sender.sendMessage("Chunk unclaimed.");
			Dynmap.removeChunk(claim);
			return true;
		}
		return false;
	}
}
