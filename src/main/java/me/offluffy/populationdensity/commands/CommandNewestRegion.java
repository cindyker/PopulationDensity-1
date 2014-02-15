package me.offluffy.populationdensity.commands;

import me.offluffy.populationdensity.utils.ConfigData;
import me.offluffy.populationdensity.utils.Lib;
import me.offluffy.populationdensity.utils.Messages;
import me.offluffy.populationdensity.utils.Messages.Message;
import me.offluffy.populationdensity.utils.PDCmd;
import me.offluffy.populationdensity.utils.PlayerHelper;
import me.offluffy.populationdensity.utils.Region;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandNewestRegion extends PDCmd {
	public CommandNewestRegion() {
		super("newestregion", "Teleport to the newest opened region", "newestregion");
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
		
		// silently fail if player is null (not online player)
		if (player == null) {
			Messages.send(player, Message.NOT_ONLINE);
			return true;
		}
		
		if(!PlayerHelper.playerCanTeleport(player, false))
			return true;
		
		//teleport the user to the open region
		Region openRegion = Region.getOpenRegion();
		PlayerHelper.teleportPlayer(player, openRegion, false);
		
		return true;
	}
}
