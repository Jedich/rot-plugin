package tld.sofugames.commands;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tld.sofugames.dao.impl.ClaimDao;
import tld.sofugames.dao.impl.DaoFactory;
import tld.sofugames.dao.impl.KingDao;
import tld.sofugames.data.*;
import tld.sofugames.models.ClaimedChunk;
import tld.sofugames.models.King;
import tld.sofugames.rot.ChunkType;

public class ClaimCommand implements CommandExecutor {

	//	public ClaimCommand(ClaimCommand instance){
//		plugin = instance;
//	}
	DaoFactory daoFactory = new DaoFactory();
	KingDao kingData = daoFactory.getKings();
	ClaimDao claimData = daoFactory.getClaims();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("claim")) {
			Player player = (Player) sender;
			String uuid = player.getUniqueId().toString();
			String chunkName = player.getLocation().getChunk().toString();
			if(!claimData.get(chunkName).isPresent()) {
				if(!kingData.get(uuid).isPresent()) {
					ClaimedChunk homeChunk = new ClaimedChunk(chunkName,
							((Player) sender).getUniqueId(), ChunkType.Home, player.getLocation().getChunk());
					claimData.save(homeChunk);
					sender.sendMessage("Let your journey begin here.");
					King thisKing = new King(player, homeChunk);
					kingData.save(thisKing);
					thisKing.setChunkNumber(thisKing.getChunkNumber() + 1);
					sender.sendMessage("Chunk successfully claimed!" + ChatColor.GOLD + " You are now a King.");
					sender.sendMessage("Please, name your kingdom with /kingdom setname <name>");
					thisKing.changeGen();
					((Player) sender).sendTitle("Glory to a new kingdom!", thisKing.getFullTitle(), 20, 100, 20);
					player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
					Data.getInstance().giveBed((Player) sender, true);
				} else {
					King king = kingData.get(uuid).get();
					if(king.getGoldBalance() >= 2) {
						ClaimedChunk claim = new ClaimedChunk(chunkName,
								((Player) sender).getUniqueId(), ChunkType.Default, player.getLocation().getChunk());
						if(isNear(claim)) {
							if(king.homeChunk.distance(claim) < 2000 * king.kingdomLevel) {
								claimData.save(claim);
								king.setChunkNumber(king.getChunkNumber() + 1);
								king.setGoldBalance(-2);
								sender.sendMessage("Chunk successfully claimed!");
							} else {
								sender.sendMessage(ChatColor.RED + "Strip claiming is forbidden!");
							}
						} else {
							sender.sendMessage(ChatColor.RED + "This chunk is not connected to your mainland!");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "Not enough money to claim!");
					}
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Chunk is already claimed.");
			}
			return true;
		}
		return false;
	}

	public boolean isNear(ClaimedChunk init/*, int depth, int initDepth*/) {
		for(Chunk ch : init.getRelatives()) {
			if(claimData.get(ch.toString()).isPresent()) {
				return true;
			}
		}
		return false;
	}
}
