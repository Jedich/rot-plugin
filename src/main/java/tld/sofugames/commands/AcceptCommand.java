package tld.sofugames.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tld.sofugames.dao.impl.ClaimDao;
import tld.sofugames.dao.impl.DaoFactory;
import tld.sofugames.data.Data;
import tld.sofugames.dao.impl.KingDao;
import tld.sofugames.models.Advisor;
import tld.sofugames.models.King;

import java.sql.SQLException;

public class AcceptCommand implements CommandExecutor {
	DaoFactory daoFactory = new DaoFactory();
	KingDao kingData = daoFactory.getKings();
	ClaimDao claimData = daoFactory.getClaims();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("accept")) {
			if(args == null || args.length == 0) {
				sender.sendMessage("§6Accept command:");
				sender.sendMessage("§6join§f: lets you join a kingdom.");
				return true;
			}
			if(args[0].equalsIgnoreCase("join")) {
				if(kingData.get(((Player) sender).getUniqueId().toString()).isPresent()) {
					sender.sendMessage("§cYou have your own lands to control!");
					return true;
				}
				if(!Data.getInstance().kingdomRequests.containsKey(((Player) sender).getUniqueId())) {
					sender.sendMessage("You have no invites pending.");
					return true;
				}
				King parent = Data.getInstance().kingdomRequests.get(((Player) sender).getUniqueId());
				Advisor advisor = new Advisor((Player) sender, parent.homeChunk, parent);
				parent.advisors.add(advisor.getUuid());
				try {
					advisor.pushToDb(Data.getInstance().getConnection());
				} catch(SQLException e) {
					e.printStackTrace();
				}
				sender.sendMessage("§aYou have joined " + parent.kingdomName + "!");
				parent.assignedPlayer.sendMessage("§aInvite accepted!");
			}
			return true;
		}
		return false;
	}
}
