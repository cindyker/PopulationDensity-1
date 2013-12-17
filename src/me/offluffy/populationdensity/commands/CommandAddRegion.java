package me.offluffy.populationdensity.commands;

import me.offluffy.populationdensity.utils.ConfigData;
import me.offluffy.populationdensity.utils.Lib;
import me.offluffy.populationdensity.utils.Messages;
import me.offluffy.populationdensity.utils.Messages.Clr;
import me.offluffy.populationdensity.utils.Messages.Message;
import me.offluffy.populationdensity.utils.PDCmd;
import me.offluffy.populationdensity.utils.Region;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandAddRegion extends PDCmd {
	public CommandAddRegion() {
		super("addregion", "Scan and add a new region", "addregion");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!Lib.perm(sender, getPerm())) {
			if (sender.isOp())
				sender.sendMessage(Messages.noPerm(getPerm()));
			else
				Messages.send(sender, Message.NO_PERM);
			return true;
		}
		
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
			if(ConfigData.managedWorld == null) {
				Messages.send(player, Message.NO_WORLD);
				return true;
			}
		}
		
		// fail if player is null (not online player)
		if (player == null) {
			Messages.send(player, Message.NOT_ONLINE);
			return true;
		}
		
		player.sendMessage(Clr.NORM + "Opened a new region and started a resource scan.");
		player.sendMessage(Clr.NORM + "See console or server logs for details.");
		
		Region newRegion = Region.addRegion();
		Region.addRegion();
		
		newRegion.scanRegion(true);
		
		return true;
	}
}
