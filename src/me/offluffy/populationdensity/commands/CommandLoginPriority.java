package me.offluffy.populationdensity.commands;

import me.offluffy.populationdensity.PopulationDensity;
import me.offluffy.populationdensity.utils.ConfigData;
import me.offluffy.populationdensity.utils.Lib;
import me.offluffy.populationdensity.utils.Messages;
import me.offluffy.populationdensity.utils.Messages.Clr;
import me.offluffy.populationdensity.utils.Messages.Message;
import me.offluffy.populationdensity.utils.PDCmd;
import me.offluffy.populationdensity.utils.PlayerData;
import me.offluffy.populationdensity.utils.PlayerHelper;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandLoginPriority extends PDCmd {
	public CommandLoginPriority() {
		super("loginpriority", "Set the specified user's login priority", "setloginpriority");
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
		
		//requires exactly two parameters, the other player's name and the priority
		if(args.length != 2 && args.length != 1)
			return false;
		
		PlayerData targetPlayerData = null;
		Player targetPlayer = null;
		if (args.length > 0) {
			//find the specified player
			targetPlayer = PopulationDensity.inst.getServer().getPlayer(args[0]);
			if(targetPlayer == null) {
				player.sendMessage(Clr.ERR + "Player \"" + args[0] + "\" not found.");
				return true;
			}
			
			targetPlayerData = PlayerHelper.getPlayerData(targetPlayer.getName());
			
			player.sendMessage(Clr.HEAD + targetPlayer.getName() + "'s " + Clr.NORM + "login priority: " + Clr.HEAD + targetPlayerData.loginPriority);
			
			if(args.length < 2)
				return false;  //usage displayed
		
			//parse the adjustment amount
			int priority;			
			try {
				priority = Integer.parseInt(args[1]);
			} catch(NumberFormatException numberFormatException) {
				return false;  //causes usage to be displayed
			}
			
			//set priority			
			if(priority > 100)
				priority = 100;
			else if(priority < 0)
				priority = 0;
			
			targetPlayerData.loginPriority = priority;
			PlayerHelper.savePlayerData(targetPlayer.getName(), targetPlayerData);
			
			//confirmation message
			player.sendMessage(Clr.ERR + "Set " + targetPlayer.getName() + "'s priority to " + priority + ".");
			
			return true;
		}
	return false;
	}
}
