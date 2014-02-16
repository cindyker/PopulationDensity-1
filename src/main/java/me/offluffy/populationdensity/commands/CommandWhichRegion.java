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

@ReflectCommand
public class CommandWhichRegion extends PDCmd {
    public CommandWhichRegion() {
        super("whichregion", "Display the name of the current region", "whichregion");
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
            if (ConfigData.managedWorld == null) {
                Messages.send(player, Message.NO_WORLD);
                return true;
            }
        }

        // fail if player is null (not online player)
        if (player == null) {
            Messages.send(player, Message.NOT_ONLINE);
            return true;
        }

        Region currentRegion = Region.fromLocation(player.getLocation());
        if (currentRegion == null) {
            Messages.send(player, Message.NO_REGION);
            return true;
        }

        String regionName = currentRegion.getName();
        if (regionName == null)
            player.sendMessage(Clr.NORM + "You're in the wilderness!  This region doesn't have a name.");
        else
            player.sendMessage(Clr.HEAD + "You're in the " + Clr.NORM + Lib.capitalize(regionName) + Clr.NORM + " region.");

        return true;
    }
}
