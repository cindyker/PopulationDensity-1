package me.offluffy.populationdensity.commands;

import me.offluffy.populationdensity.utils.ConfigData;
import me.offluffy.populationdensity.utils.Lib;
import me.offluffy.populationdensity.utils.Messages;
import me.offluffy.populationdensity.utils.Messages.Clr;
import me.offluffy.populationdensity.utils.Messages.Message;
import me.offluffy.populationdensity.utils.PDCmd;
import me.offluffy.populationdensity.utils.PlayerData;
import me.offluffy.populationdensity.utils.PlayerHelper;
import me.offluffy.populationdensity.utils.Region;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHomeRegion extends PDCmd {
	public CommandHomeRegion() {
		super("homeregion", "Return to your home region", "hometeleport");
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
		PlayerData playerData = null;
		if (sender instanceof Player) {
			player = (Player) sender;
			if(ConfigData.managedWorld == null) {
				Messages.send(player, Message.NO_WORLD);
				return true;
			}
			playerData = PlayerHelper.getPlayerData(player.getName());
		}
		
		// fail if player is null (not online player)
		if (player == null) {
			Messages.send(player, Message.NOT_ONLINE);
			return true;
		}
		
		//check to ensure the player isn't already home
		Region homeRegion = playerData.homeRegion;
		if(!player.hasPermission("populationdensity.teleportanywhere") && !ConfigData.teleportFromAnywhere && homeRegion.equals(Region.fromLocation(player.getLocation()))) {
			player.sendMessage(Clr.ERR + "You're already in your home region.");
			return true;
		}
		
		//consider config, player location, player permissions
		if(PlayerHelper.playerCanTeleport(player, true)) {
			PlayerHelper.teleportPlayer(player, homeRegion, false);
			return true;
		}
		
		return true;
	}
}