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
import me.offluffy.populationdensity.utils.Region;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandInviteToRegion extends PDCmd {
	public CommandInviteToRegion() {
		super("invitetoregion", "Invite a player to your region", "invitetoregion");
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
		
		if(args.length < 1)
			return false;
		
		//figure out the player's home region
		Region homeRegion = playerData.homeRegion;
		
		//send a notification to the invitee, if he's available
		Player invitee = PopulationDensity.inst.getServer().getPlayer(args[0]);
		if(invitee != null) {
			if (invitee.equals(player)) {
				player.sendMessage(Clr.ERR + "You can't invite yourself!");
				return true;
			}
			
			if (!Lib.perm(invitee, "populationdensity.acceptinvite")) {
				player.sendMessage(Clr.ERR + "This player doesn't have permission to accept invites");
				return true;
			}
			
			playerData = PlayerHelper.getPlayerData(invitee.getName());
			playerData.regionInvitation = homeRegion;
			player.sendMessage(Clr.NORM + "Invitation sent.  " + Clr.HEAD + invitee.getName() + Clr.NORM + " must use a region post to teleport to your region.");
			
			invitee.sendMessage(Clr.HEAD + player.getName() + Clr.NORM + " has invited you to visit his or her home region!");
			invitee.sendMessage(Clr.NORM + "Stand near a region post and use " + Clr.HEAD + "/AcceptRegionInvite" + Clr.NORM + " to accept.");
		} else {
			player.sendMessage(Clr.ERR + "There's no player named \"" + Clr.HEAD + args[0] + Clr.NORM + "\" online right now.");
		}
		
		return true;
	}
}
