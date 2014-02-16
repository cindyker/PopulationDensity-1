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

@ReflectCommand
public class CommandMoveIn extends PDCmd {
    public CommandMoveIn() {
        super("movein", "Move in to your current region", "movein");
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
            if (ConfigData.managedWorld == null) {
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

        //if not in the managed world, /movein doesn't make sense
        if (!player.getWorld().equals(ConfigData.managedWorld)) {
            player.sendMessage(Clr.ERR + "Sorry, no one can move in here.");
            return true;
        }

        Region currentRegion = Region.fromLocation(player.getLocation());

        if (currentRegion.equals(playerData.homeRegion)) {
            player.sendMessage(Clr.ERR + "This region is already your home!");
            return true;
        }

        playerData.homeRegion = Region.fromLocation(player.getLocation());
        PlayerHelper.savePlayerData(player.getName(), playerData);
        player.sendMessage(Clr.NORM + "Welcome to your new home!");
        player.sendMessage(Clr.HEAD + "/HomeRegion " + Clr.NORM + "Return here");
        player.sendMessage(Clr.HEAD + "/InviteToRegion " + Clr.NORM + "Invite other players here.");

        return true;
    }
}
