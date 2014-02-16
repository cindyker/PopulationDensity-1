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
public class CommandScanRegion extends PDCmd {
    public CommandScanRegion() {
        super("scanregion", "Scan the current region for resources", "addregion");
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

        player.sendMessage(Clr.NORM + "Started scan.  Check console or server logs for results.");
        Region.fromLocation(player.getLocation()).scanRegion(false);

        return true;
    }
}
