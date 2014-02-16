package me.offluffy.populationdensity.commands;

import me.offluffy.populationdensity.utils.ConfigData;
import me.offluffy.populationdensity.utils.Lib;
import me.offluffy.populationdensity.utils.Messages;
import me.offluffy.populationdensity.utils.Messages.Clr;
import me.offluffy.populationdensity.utils.Messages.Message;
import me.offluffy.populationdensity.utils.PDCmd;
import me.offluffy.populationdensity.utils.PlayerHelper;
import me.offluffy.populationdensity.utils.Region;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@ReflectCommand
public class CommandVisitRegion extends PDCmd {
    public CommandVisitRegion() {
        super("visitregion", "Visit the specified region", "visitregion");
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

        if (args.length < 1)
            return false;

        //find the specified region, and send an error message if it's not found
        Region region = Region.getRegion(args[0].toLowerCase());
        if (region == null) {
            player.sendMessage(Clr.ERR + "There's no region named \"" + args[0] + "\".  Unable to teleport.");
            return true;
        }

        if (!PlayerHelper.playerCanTeleport(player, false))
            return true;

        //otherwise, teleport the user to the specified region
        PlayerHelper.teleportPlayer(player, region, false);

        return true;
    }
}
