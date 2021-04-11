package tld.sofugames.commands;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tld.sofugames.data.Data;
import tld.sofugames.models.ClaimedChunk;
import tld.sofugames.models.King;
import tld.sofugames.rot.ChunkType;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;

public class ClaimCommand implements CommandExecutor {

//	public ClaimCommand(ClaimCommand instance){
//		plugin = instance;
//	}


	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("claim")) {
			Player player = (Player) sender;
			String uuid = player.getUniqueId().toString();
			String chunkName = player.getLocation().getChunk().toString();
			if (!Data.getInstance().claimData.containsKey(chunkName)) {
				if (!Data.getInstance().kingData.containsKey(uuid)) {

					ClaimedChunk homeChunk = new ClaimedChunk(Data.getInstance().getLastClaim(), chunkName,
							((Player) sender).getUniqueId(), ChunkType.Home, player.getLocation().getChunk());
					try {
						homeChunk.pushToDb(Data.getInstance().getConnection());
					} catch (SQLException e) {
						e.printStackTrace();
						return false;
					}
					sender.sendMessage("Let your journey begin here.");
					try {
						Data.getInstance().claimData.put(player.getLocation().getChunk().toString(), homeChunk);
						King thisKing = new King(Data.getInstance().getLastKing(), player, homeChunk);
						Data.getInstance().kingData.put(uuid, thisKing);
						if (thisKing.pushToDb(Data.getInstance().getConnection())) {
							thisKing.chunkNumber++;
							sender.sendMessage("Chunk successfully claimed!" + ChatColor.GOLD + " You are now a King.");
							sender.sendMessage("Please, name your kingdom with /kingdom setname [NAME]");
							thisKing.changeGen();
							((Player) sender).sendTitle("Glory to a new kingdom!", thisKing.fullTitle, 20, 100 ,20);
							player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
							Data.getInstance().giveBed((Player) sender, true);
						}
					} catch (SQLException e) {
						e.printStackTrace();
						sender.sendMessage(ChatColor.RED + "Internal server error.");
					}
				} else {
					try {
						ClaimedChunk claim = new ClaimedChunk(Data.getInstance().getLastClaim(), chunkName,
								((Player) sender).getUniqueId(), ChunkType.Default, player.getLocation().getChunk());
						King king = Data.getInstance().kingData.get(uuid);
						System.out.println(king.homeChunk.distance(claim));
						if(isNear(claim)) {
							if (king.homeChunk.distance(claim) < 5 * king.kingdomLevel) {
								king.updateInDb(Data.getInstance().getConnection(), Collections.singletonMap("chunk_number", king.chunkNumber));
								Data.getInstance().claimData.put(player.getLocation().getChunk().toString(), claim);
								claim.pushToDb(Data.getInstance().getConnection());
								king.chunkNumber++;
								sender.sendMessage("Chunk successfully claimed!");
							} else {
								sender.sendMessage(ChatColor.RED + "Strip claiming is forbidden!");
							}
						} else {
							sender.sendMessage(ChatColor.RED + "This chunk is not connected to your mainland!");
						}
					} catch (SQLException e) {
						e.printStackTrace();
						sender.sendMessage(ChatColor.RED + "Internal server error.");
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
		for (Chunk ch : init.getRelatives()) {
			if (Data.getInstance().claimData.containsKey(ch.toString())) {
				return true;
			}
		}
		return false;
		//following shit is too harmful for server memory
//		if (depth < 0) return false;
//		if (initDepth == depth && init.getRelativeNumber(init.world) > 1) {
//			System.out.println("aoo here are " + init.getRelativeNumber(init.world) + " neighbors");
//			return true;
//		}
//		for (Chunk ch : init.getRelatives()) {
//			if (Data.getInstance().claimData.containsKey(ch.toString())) {
//				ClaimedChunk claimNeighbor = Data.getInstance().claimData.get(ch.toString());
//				if (claimNeighbor.type == ChunkType.Home) {
//					return true;
//				}
//				int relNumber = claimNeighbor.getRelativeNumber(init.world);
//				System.out.println("here are " + relNumber + " neighbors");
//				if (relNumber > 1) {
//					return true;
//				} else {
//					System.out.println("deeper, to " + (depth - 1) + ", rel: " + relNumber);
//					return isNear(claimNeighbor, depth - 1, initDepth);
//				}
//			}
//		}
//		return false;
	}
}
